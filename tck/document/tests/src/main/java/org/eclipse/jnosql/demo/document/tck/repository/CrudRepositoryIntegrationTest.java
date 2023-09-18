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

import com.github.javafaker.Faker;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.demo.document.tck.model.Person;
import org.eclipse.jnosql.demo.document.tck.model.PersonRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;

public interface CrudRepositoryIntegrationTest {

    static Faker faker = new Faker();

    @Test
    default void shouldSaveEntity(PersonRepository personRepository) {
        assertSoftly(softly -> {
            Person entity = createRandomPerson();
            Person persistedEntity = personRepository.save(entity);
            softly.assertThat(persistedEntity)
                    .as("when a new entity is saved then the returned entity should be non-null and non-empty")
                    .isNotNull();
            checkSaveEntity(softly, personRepository, entity);

            // updating persisted entity
            persistedEntity.setName(faker.name().fullName());
            var updatedEntity = personRepository.save(persistedEntity);
            Optional<Person> updatedPersonReference = personRepository.findById(updatedEntity.getId());
            softly.assertThat(updatedPersonReference)
                    .as("when a persisted entity is changed then the returned entity should be non-null and non-empty")
                    .isNotNull()
                    .isNotEmpty();

            updatedPersonReference.ifPresent(returnedPerson -> {
                checkEntityEquality(softly, returnedPerson, updatedEntity);
            });
        });
    }

    @Test
    default void shouldSaveAllEntities(PersonRepository personRepository) {
        assertSoftly(softly -> {
            var entities = List.of(createRandomPerson(), createRandomPerson(), createRandomPerson());
            var persistedEntities = personRepository.saveAll(entities);
            softly.assertThat(persistedEntities)
                    .as("when new entities are save then the returned list should be not null")
                    .isNotNull()
                    .as("when new entities are save then the returned list should be no-empty one")
                    .isNotEmpty()
                    .as("the returned list should have the same size of the entity list provided")
                    .hasSize(entities.size());
            entities.forEach(entity -> checkSaveEntity(softly, personRepository, entity));
        });
    }

    default void checkSaveEntity(SoftAssertions softly,
                                 PersonRepository personRepository,
                                 Person entity) {

        Optional<Person> personReference = personRepository.findById(entity.getId());

        softly.assertThat(personReference)
                .as("when a new entity is saved then the returned entity should be non-null and non-empty")
                .isNotNull()
                .isNotEmpty();

        personReference.ifPresent(returnedPerson -> {
            checkEntityEquality(softly, returnedPerson, entity);
        });
    }

    default void checkEntityEquality(SoftAssertions softly,
                                     Person returnedEntity,
                                     Person entity) {
        softly.assertThat(returnedEntity.getId())
                .as("when a new entity is saved then the returned entity should have the same id")
                .isEqualTo(entity.getId());
        softly.assertThat(returnedEntity.getName())
                .as("the returned entity should have the same name")
                .isEqualTo(entity.getName());
        softly.assertThat(returnedEntity.getBirthday())
                .as("the returned entity should have the same birthday")
                .isEqualTo(entity.getBirthday());
    }

    default Person createRandomPerson() {
        return Person.create(
                faker.name().fullName(),
                LocalDate.ofInstant(faker.date().birthday().toInstant(), ZoneId.systemDefault()));
    }

    @Test
    default void shouldDeleteAll(PersonRepository personRepository) {
        assertSoftly(softAssertions -> {
            var entities = List.of(createRandomPerson(), createRandomPerson(), createRandomPerson());
            personRepository.saveAll(entities);
            await().until(() -> personRepository.count() >= entities.size());
            personRepository.deleteAll();
            await().atLeast(Duration.of(2, ChronoUnit.SECONDS));
            softAssertions.assertThat(personRepository.count())
                    .as("should delete all persisted entities")
                    .isZero();
        });
    }

    default void deleteAllPerson(PersonRepository personRepository) {
        assertSoftly(softly -> {
            personRepository.deleteAll();
            softly.assertThat(personRepository.count())
                    .as("it's expected to count is zero after delete all persisted entities").isZero();
        });
    }

    @Test
    default void shouldCount(PersonRepository personRepository) {
        deleteAllPerson(personRepository);
        assertSoftly(softly -> {
            personRepository.save(createRandomPerson());
            softly.assertThat(personRepository.count())
                    .as("it's expected to count is positive after delete all persisted entities").isPositive();
        });
    }
}
