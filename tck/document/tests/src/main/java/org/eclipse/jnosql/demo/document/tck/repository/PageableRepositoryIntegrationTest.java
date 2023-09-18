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
package org.eclipse.jnosql.demo.document.tck.repository;

import jakarta.data.repository.Page;
import jakarta.data.repository.Pageable;
import org.eclipse.jnosql.demo.document.tck.model.Person;
import org.eclipse.jnosql.demo.document.tck.model.PersonRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

public interface PageableRepositoryIntegrationTest extends CrudRepositoryIntegrationTest {

    @Test
    default void shouldFindAllPaginated(PersonRepository personRepository) {
        deleteAllPerson(personRepository);

        var personList = personRepository
                .saveAll(List.of(createRandomPerson(), createRandomPerson(), createRandomPerson(), createRandomPerson()));

        Pageable pageable1 = Pageable.ofSize(2).page(1);
        Pageable pageable2 = pageable1.next();

        Page<Person> page1 = personRepository.findAll(pageable1);
        Page<Person> page2 = personRepository.findAll(pageable2);


        deleteAllPerson(personRepository);
    }
}
