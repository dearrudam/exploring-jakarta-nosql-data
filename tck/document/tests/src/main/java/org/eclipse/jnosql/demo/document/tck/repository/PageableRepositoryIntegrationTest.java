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
package org.eclipse.jnosql.demo.document.tck.repository;

import com.github.javafaker.Faker;
import org.eclipse.jnosql.demo.document.tck.model.Person;
import org.eclipse.jnosql.demo.document.tck.model.PersonRepository;
import org.eclipse.jnosql.mapping.Convert;
import org.eclipse.jnosql.mapping.Converters;
import org.eclipse.jnosql.mapping.document.DocumentEntityConverter;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;

import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.spi.EntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages(value = {Convert.class, Converters.class, DocumentEntityConverter.class})
@AddPackages(Person.class)
@AddPackages(Reflections.class)
@AddExtensions({EntityMetadataExtension.class, DocumentExtension.class})
public interface PageableRepositoryIntegrationTest {

    static Faker faker = new Faker();

    @Test
    default void shouldSaveEntity(PersonRepository personRepository) {
        assertSoftly(softly -> {
            Person entity = createRandomPerson();
            var persistedEntity = personRepository.save(entity);

            softly.assertThat(persistedEntity)
                    .as("the returned entity should be non-null")
                    .isNotNull()
                    .isEqualTo(entity);

            Optional<Person> personReference = personRepository.findById(persistedEntity.getId());

            softly.assertThat(personReference)
                    .as("when a new entity is saved then the returned entity should be non-null and non-empty")
                    .isNotNull()
                    .isNotEmpty();

            personReference.ifPresent(returnedPerson -> {
                softly.assertThat(returnedPerson.getId())
                        .as("when a new entity is saved then the returned entity should have the same id")
                        .isEqualTo(entity.getId());
                softly.assertThat(returnedPerson.getName())
                        .as("the returned entity should have the same name")
                        .isEqualTo(entity.getName());
                softly.assertThat(returnedPerson.getBirthday())
                        .as("the returned entity should have the same birthday")
                        .isEqualTo(entity.getBirthday());
            });

            persistedEntity.setName(faker.name().fullName());

            var updatedEntity = personRepository.save(persistedEntity);

            Optional<Person> updatedPersonReference = personRepository.findById(updatedEntity.getId());

            softly.assertThat(updatedPersonReference)
                    .as("when a persisted entity is changed then the returned entity should be non-null and non-empty")
                    .isNotNull()
                    .isNotEmpty();

            updatedPersonReference.ifPresent(returnedPerson -> {
                softly.assertThat(returnedPerson.getId())
                        .as("the returned entity should have the same id that persisted entity")
                        .isEqualTo(updatedEntity.getId());
                softly.assertThat(returnedPerson.getName())
                        .as("the returned entity should have the same name that persisted entity")
                        .isEqualTo(updatedEntity.getName());
                softly.assertThat(returnedPerson.getBirthday())
                        .as("the returned entity should have the same birthday that persisted entity")
                        .isEqualTo(updatedEntity.getBirthday());
            });
        });
    }

    default Person createRandomPerson() {
        return Person.create(
                faker.name().fullName(),
                LocalDate.ofInstant(faker.date().birthday().toInstant(), ZoneId.systemDefault()));
    }


    @Test
    default void shouldCount(PersonRepository personRepository) {
        assertSoftly(softly -> {
            personRepository.deleteAll();
            softly.assertThat(personRepository.count()).isZero();

            personRepository.save(createRandomPerson());
            softly.assertThat(personRepository.count()).isPositive();
        });
    }
}
