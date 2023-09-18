/*
 *    Copyright (c) 2023 Contributors to the Eclipse Foundation
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License v1.0
 *    and Apache License v2.0 which accompanies this distribution.
 *    The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *    and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *    You may elect to redistribute this code under either of these licenses.
 *
 *    Contributors:
 *
 *    Maximillian Arruda
 */

package org.eclipse.jnosql.demo.document.tck.couchbase;

import com.couchbase.client.core.error.BucketNotFoundException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.manager.bucket.BucketManager;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.manager.collection.CollectionManager;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.manager.collection.ScopeSpec;
import com.couchbase.client.java.manager.query.QueryIndexManager;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.SettingsBuilder;
import org.eclipse.jnosql.databases.couchbase.communication.*;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions;
import static com.couchbase.client.java.manager.query.GetAllQueryIndexesOptions.getAllQueryIndexesOptions;

public enum Database {

    INSTANCE;

    private CouchbaseContainer container;
    private CouchbaseSettings settings;
    private final BucketDefinition bucketDefinition;
    private final CustomCouchbaseDocumentConfiguration configuration;
    private final Settings fileSettings;

    Database() {

        this.fileSettings = CouchbaseUtil.getSettings();

        this.configuration = new CustomCouchbaseDocumentConfiguration();

        this.bucketDefinition = new BucketDefinition(CouchbaseUtil.BUCKET_NAME)
                .withFlushEnabled(true);

        this.start();
    }

    static class CustomCouchbaseDocumentConfiguration extends CouchbaseDocumentConfiguration {
        @Override
        public void update(Settings settings) {
            super.update(settings);
        }

        @Override
        public String getHost(Settings settings) {
            return super.getHost(settings);
        }

        @Override
        public String getUser(Settings settings) {
            return super.getUser(settings);
        }

        @Override
        public String getPassword(Settings settings) {
            return super.getPassword(settings);
        }
    }

    public void start() {
        if (container != null && container.isRunning()) {
            return;
        }
        container = new CouchbaseContainer("couchbase/server")
                .withBucket(bucketDefinition)
                .withCredentials(configuration.getUser(fileSettings), configuration.getPassword(fileSettings))
                .withReuse(true);

        container.start();

        configuration.update(fileSettings);

        setup(configuration, configuration.toCouchbaseSettings());

        settings = configuration.toCouchbaseSettings();

        setUp(settings, bucketDefinition.getName());
    }

    public void stop() {
        container.stop();
    }

    private void setUp(CouchbaseSettings settings, String database) {

        Objects.requireNonNull(database, "database is required");

        var collections = settings.getCollections().stream().map(String::trim)
                .filter(index -> !index.isBlank()).toList();

        var collectionsToIndex = Arrays
                .stream(Optional.ofNullable(settings.getIndex()).orElse("").split(","))
                .map(String::trim)
                .filter(index -> !index.isBlank()).collect(Collectors.toSet());

        var scope = settings.getScope();

        long start = System.currentTimeMillis();
        System.out.println("starting the setup with database: " + database);

        try (Cluster cluster = settings.getCluster()) {

            BucketManager buckets = cluster.buckets();
            try {
                buckets.getBucket(database);
            } catch (BucketNotFoundException exp) {
                System.out.println("The database/bucket does not exist, creating it: " + database);
                buckets.createBucket(BucketSettings.create(database));
            }

            Bucket bucket = cluster.bucket(database);

            waitUntilReady(bucket);

            CollectionManager manager = bucket.collections();

            List<ScopeSpec> scopes = manager.getAllScopes();
            String finalScope = scope.orElseGet(() -> bucket.defaultScope().name());
            ScopeSpec spec = scopes.stream().filter(s -> finalScope.equals(s.name()))
                    .findFirst().get();

            collectionsToIndex.forEach(collection -> {
                if (spec.collections().stream().noneMatch(c -> collectionsToIndex.contains(c.name()))) {
                    manager.createCollection(CollectionSpec.create(collection, finalScope));
                }
            });

            waitUntilReady(bucket);

            if (!collectionsToIndex.isEmpty()) {
                QueryIndexManager queryIndexManager = cluster.queryIndexes();
                collections.stream()
                        .filter(collectionsToIndex::contains)
                        .forEach(collection -> {
                            var allIndexes = queryIndexManager.getAllIndexes(database, getAllQueryIndexesOptions()
                                    .scopeName(finalScope).collectionName(collection));
                            if (allIndexes.isEmpty()) {
                                System.out.println("Index for " + collection + " collection does not exist, creating primary key with scope "
                                        + finalScope + " collection " + collection + " at database " + database);
                                queryIndexManager.createPrimaryIndex(database, createPrimaryQueryIndexOptions()
                                        .scopeName(finalScope).collectionName(collection));
                            }
                        });
            }

            long end = System.currentTimeMillis() - start;
            System.out.println("Finished the setup with database: " + database + " end with millis "
                    + end);
        }

    }

    private static void waitUntilReady(Bucket bucket) {
        bucket.waitUntilReady(Duration.of(4, ChronoUnit.SECONDS));
    }

    public CouchbaseDocumentConfiguration getDocumentConfiguration() {
        CouchbaseDocumentConfiguration configuration = setup(new CouchbaseDocumentConfiguration());
        return configuration;
    }

    public CouchbaseKeyValueConfiguration getKeyValueConfiguration() {
        CouchbaseKeyValueConfiguration configuration = setup(new CouchbaseKeyValueConfiguration());
        return configuration;
    }

    private <T extends CouchbaseConfiguration> T setup(T configuration) {
        return setup(configuration, this.settings);
    }

    private <T extends CouchbaseConfiguration> T setup(T configuration, CouchbaseSettings settings) {
        configuration.setHost(container.getConnectionString());
        configuration.setUser(container.getUsername());
        configuration.setPassword(container.getPassword());
        settings.getCollection().ifPresent(configuration::setCollection);
        settings.getCollections().stream().toList().forEach(configuration::addCollection);
        settings.getScope().ifPresent(configuration::setScope);
        return configuration;
    }

    public CouchbaseSettings getCouchbaseSettings() {
        return settings;
    }

    public Settings getSettings() {
        return getSettingsBuilder().build();
    }

    public SettingsBuilder getSettingsBuilder() {
        CouchbaseSettings couchbaseSettings = getCouchbaseSettings();
        SettingsBuilder builder = CouchbaseUtil.getSettingsBuilder();
        builder.put(CouchbaseConfigurations.HOST.get(), couchbaseSettings.getHost());
        couchbaseSettings.getScope().ifPresent(scope ->
                builder.put(CouchbaseConfigurations.SCOPE.get(), scope));
        Optional.ofNullable(couchbaseSettings.getIndex()).ifPresent(index ->
                builder.put(CouchbaseConfigurations.INDEX.get(), index));
        if (!couchbaseSettings.getCollections().isEmpty()) {
            builder.put(CouchbaseConfigurations.COLLECTIONS.get(),
                    String.join(",", couchbaseSettings.getCollections()));
        }
        Optional.ofNullable(couchbaseSettings.getUser()).ifPresent(user ->
                builder.put(CouchbaseConfigurations.USER.get(), couchbaseSettings.getUser()));
        Optional.ofNullable(couchbaseSettings.getPassword()).ifPresent(password ->
                builder.put(CouchbaseConfigurations.PASSWORD.get(), password));
        return builder;
    }

}
