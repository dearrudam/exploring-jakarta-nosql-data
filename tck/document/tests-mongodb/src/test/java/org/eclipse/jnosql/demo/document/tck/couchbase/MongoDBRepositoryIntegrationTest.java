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

import org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentConfigurations;
import org.eclipse.jnosql.databases.mongodb.mapping.MongoDBTemplate;
import org.eclipse.jnosql.demo.document.tck.repository.PageableRepositoryIntegrationTest;
import org.eclipse.jnosql.mapping.config.MappingConfigurations;
import org.jboss.weld.junit5.auto.AddPackages;

import static org.eclipse.jnosql.demo.document.tck.couchbase.MongoDatabase.INSTANCE;

@AddPackages(MongoDBTemplate.class)
public class MongoDBRepositoryIntegrationTest implements PageableRepositoryIntegrationTest {

    static {
        INSTANCE.get("library");
        System.setProperty(MongoDBDocumentConfigurations.HOST.get() + ".1", INSTANCE.host());
        System.setProperty(MappingConfigurations.DOCUMENT_DATABASE.get(), "library");
    }
}