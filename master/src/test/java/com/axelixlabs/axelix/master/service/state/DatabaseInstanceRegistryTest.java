/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.state;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.JavaVersion;
import com.axelixlabs.axelix.master.domain.MemoryUsage;
import com.axelixlabs.axelix.master.repository.InstanceRepository;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static com.axelixlabs.axelix.master.utils.TestInstanceFactory.withName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Base class for integration tests of {@link DatabaseInstanceRegistry}
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
// TODO:
//  Checks for an Instant fields are not performed in these tests, they are omitted there.
//  And I think this can lead to problems in the future. AssertJ provides the way to test the
//  Instant and other datetime fields with closeTo method that we should probably utilize
@DatabaseMatrixTest
class DatabaseInstanceRegistryTest {

    @Autowired
    private InstanceRegistry instanceRegistry;

    @Autowired
    private InstanceRepository instanceRepository;

    @BeforeEach
    @AfterEach
    void setup() {
        instanceRepository.deleteAll();
    }

    @Test
    void reload_shouldPersistInstance() {

        // given.
        Instant instant = Instant.now();
        Instance instance = new Instance(
                InstanceId.of("test-id-1"),
                ApplicationId.of("com.axelixlabs", "test-app"),
                "name",
                "1.0.0",
                "1.0.0-SNAPSHOT",
                new JavaVersion(17, 0, 0, 0),
                "SB-3",
                "Spring-6",
                "2.0.0",
                "BellSoft",
                "sha-commit",
                instant,
                instant,
                Instance.InstanceStatus.UP,
                new MemoryUsage(1234d),
                "Http://localhost:8080/actuator");

        // when.
        instanceRegistry.register(instance);

        // then.
        Optional<Instance> expectedInstance = instanceRegistry.get(InstanceId.of("test-id-1"));
        assertThat(expectedInstance).isPresent();

        // When persisting an Instant, either the Spring Data truncates the Instant (which has
        // nano time precision) to the microseconds or something.
        assertThat(expectedInstance.get())
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(instance);
    }

    @Test
    void reload_shouldUpdateExistingInstance() {
        // given.
        Instant instant = Instant.now();
        Instance instance = new Instance(
                InstanceId.of("test-id-2"),
                ApplicationId.of("com.axelixlabs", "test-app"),
                "name",
                "1.0.0",
                "1.0.0-SNAPSHOT",
                new JavaVersion(17, 0, 0, 0),
                "SB-3",
                "Spring-6",
                "2.0.0",
                " BellSoft",
                "sha",
                instant,
                instant,
                Instance.InstanceStatus.UP,
                new MemoryUsage(1234d),
                "Http://localhost:8080/actuator");
        instanceRegistry.register(instance);

        // when.
        Instance updated = new Instance(
                instance.id(),
                instance.applicationId(),
                "updated-name",
                "1.0.1",
                "1.0.1-SNAPSHOT",
                new JavaVersion(21, 0, 0, 0),
                "SB-4",
                "Spring-7",
                "2.2.0",
                "Axiom JDK",
                "new-sha",
                instant,
                Instant.now(),
                Instance.InstanceStatus.DOWN,
                new MemoryUsage(1200d),
                instance.actuatorUrl());

        instanceRegistry.register(updated);

        // then.
        Optional<Instance> found = instanceRegistry.get(InstanceId.of("test-id-2"));
        assertThat(found).isPresent();

        // When persisting an Instant, either the Spring Data truncates the Instant (which has
        // nano time precision) to the microseconds or something.
        assertThat(found.get())
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(updated);
    }

    @Test
    void refresh_shouldUpdateStatusHeapAndHeartBeatOnly() {
        // given.
        Instant staleHeartBeat = Instant.now().minus(1, ChronoUnit.HOURS);
        Instance instance = TestInstanceFactory.createInstanceWithHeartbeat("refresh-id-1", staleHeartBeat);
        Instance untouched = TestInstanceFactory.createInstanceWithHeartbeat("refresh-id-2", staleHeartBeat);
        instanceRegistry.register(instance);
        instanceRegistry.register(untouched);

        // when.
        instanceRegistry.refresh(instance.id(), Instance.InstanceStatus.DOWN, new MemoryUsage(2048d));

        // then.
        Instance refreshed = instanceRegistry.get(instance.id()).orElseThrow();
        assertThat(refreshed.status()).isEqualTo(Instance.InstanceStatus.DOWN);
        assertThat(refreshed.memoryUsage()).isEqualTo(new MemoryUsage(2048d));
        assertThat(refreshed.latestHeartBeat()).isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES));
        assertThat(refreshed)
                .usingRecursiveComparison()
                .ignoringFields("status", "memoryUsage")
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(instance);

        // and the other instance is left as it was.
        Instance other = instanceRegistry.get(untouched.id()).orElseThrow();
        assertThat(other.status()).isEqualTo(untouched.status());
        assertThat(other.memoryUsage()).isEqualTo(untouched.memoryUsage());
        assertThat(other.latestHeartBeat()).isCloseTo(staleHeartBeat, within(1, ChronoUnit.SECONDS));
    }

    @Test
    void refresh_shouldNotCreateUnknownInstance() {
        // when.
        instanceRegistry.refresh(InstanceId.of("unknown-id"), Instance.InstanceStatus.UP, new MemoryUsage(2048d));

        // then.
        assertThat(instanceRegistry.getAll()).isEmpty();
    }

    @Test
    void registerAll_shouldPersistAllInstances() {
        // given.
        List<Instance> instances = List.of(
                TestInstanceFactory.create("batch-id-1"),
                TestInstanceFactory.create("batch-id-2"),
                TestInstanceFactory.create("batch-id-3"));

        // when
        instanceRegistry.register(instances);

        // then.
        assertThat(instanceRegistry.getAll()).hasSize(3);
    }

    @Test
    void deRegister_shouldRemoveInstance() {
        // given.
        Instance instance = TestInstanceFactory.create("deregister-id-1");
        instanceRegistry.register(instance);
        assertThat(instanceRegistry.get(InstanceId.of("deregister-id-1"))).isNotEmpty();

        // when.
        instanceRegistry.deRegister(InstanceId.of("deregister-id-1"));

        // then.
        assertThat(instanceRegistry.get(InstanceId.of("deregister-id-1"))).isEmpty();
    }

    @Test
    void deRegisterAll_shouldRemoveAllInstances() {
        // given.
        instanceRegistry.register(TestInstanceFactory.create("deregister-all-1"));
        instanceRegistry.register(TestInstanceFactory.create("deregister-all-2"));

        assertThat(instanceRegistry.getAll()).hasSize(2);

        // when.
        instanceRepository.deleteAllById(List.of(InstanceId.of("deregister-all-1"), InstanceId.of("deregister-all-2")));

        // then.
        assertThat(instanceRepository.findAll()).isEmpty();
    }

    @Test
    void getAll_shouldReturnAllInstances() {
        // given.
        instanceRegistry.register(TestInstanceFactory.create("test-id-1"));
        instanceRegistry.register(TestInstanceFactory.create("test-id-2"));

        // when. / then.
        assertThat(instanceRegistry.getAll())
                .extracting(Instance::id)
                .containsOnly(InstanceId.of("test-id-1"), InstanceId.of("test-id-2"));
    }

    @Test
    void findByQuery_shouldReturnMatchingInstances() {
        // given.
        Instance petclinicInstance = withName("query-id-1", "petclinic-service");
        Instance featureServiceInstance = withName("query-id-2", "feature-service");

        instanceRegistry.register(petclinicInstance);
        instanceRegistry.register(featureServiceInstance);

        // when.
        Set<Instance> result = instanceRegistry.findByQuery("petclinic");
        assertThat(result).hasSize(1);

        // then.
        // When persisting an Instant, either the Spring Data truncates the Instant (which has
        // nano time precision) to the microseconds or something.
        assertThat(result.iterator().next())
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(petclinicInstance);
    }
}
