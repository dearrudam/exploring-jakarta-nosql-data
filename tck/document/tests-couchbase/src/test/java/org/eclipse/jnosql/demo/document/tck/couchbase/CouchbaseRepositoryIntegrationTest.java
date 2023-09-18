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

import org.eclipse.jnosql.databases.couchbase.mapping.CouchbaseExtension;
import org.eclipse.jnosql.databases.couchbase.mapping.CouchbaseTemplate;
import org.eclipse.jnosql.demo.document.tck.repository.PageableRepositoryIntegrationTest;
import org.eclipse.jnosql.mapping.Convert;
import org.eclipse.jnosql.mapping.Converters;
import org.eclipse.jnosql.mapping.document.DocumentEntityConverter;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.spi.EntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;

@EnableAutoWeld
@AddPackages(value = {Convert.class, Converters.class, DocumentEntityConverter.class})
@AddPackages(Reflections.class)
@AddExtensions({EntityMetadataExtension.class, DocumentExtension.class})
@AddPackages(CouchbaseTemplate.class)
@AddExtensions({CouchbaseExtension.class})
public class CouchbaseRepositoryIntegrationTest implements PageableRepositoryIntegrationTest {

    static {
        CouchbaseUtil.systemPropertySetup();
    }

}
