/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *
 * Maximillian Arruda
 *
 */

package org.eclipse.jnosql.demo.document.tck.couchbase;

import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.SettingsBuilder;
import org.eclipse.jnosql.databases.couchbase.communication.*;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;

import java.util.Optional;

public enum Database {

    INSTANCE;

    public static final String COUCHBASE_IMAGE_NAME = "couchbase/server";
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
        container = new CouchbaseContainer(COUCHBASE_IMAGE_NAME)
                .withBucket(bucketDefinition)
                .withCredentials(configuration.getUser(fileSettings), configuration.getPassword(fileSettings));

        container.start();

        configuration.update(fileSettings);

        setup(configuration, configuration.toCouchbaseSettings());

        settings = configuration.toCouchbaseSettings();

        settings.setUp(bucketDefinition.getName());
    }

    public void stop() {
        container.stop();
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
