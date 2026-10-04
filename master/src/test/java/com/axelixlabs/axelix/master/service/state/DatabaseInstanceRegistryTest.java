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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.JavaVersion;
import com.axelixlabs.axelix.master.domain.MemoryUsage;
import com.axelixlabs.axelix.master.domain.ProbeState.InstanceKey;
import com.axelixlabs.axelix.master.repository.InstanceRepository;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static com.axelixlabs.axelix.master.utils.TestInstanceFactory.withName;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

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
                "Http://localhost:8080/actuator",
                null);

        // when.
        instanceRegistry.reload(instance);

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
                "Http://localhost:8080/actuator",
                null);
        instanceRegistry.reload(instance);

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
                instance.actuatorUrl(),
                null);

        instanceRegistry.reload(updated);

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
    void reconcile_shouldReplaceFreshKeepRetainedAndRemoveVanishedInstances() {
        // given.
        InstanceKey freshKey = new InstanceKey("service", "10.0.0.1", 8080);
        InstanceKey retainedKey = new InstanceKey("service", "10.0.0.2", 8080);
        InstanceKey vanishedKey = new InstanceKey("service", "10.0.0.3", 8080);

        jdbcAggregateTemplate.insertAll(List.of(
                discovered("fresh-id", freshKey),
                discovered("retained-id", retainedKey),
                discovered("vanished-id", vanishedKey)));

        Instance updated = discovered("fresh-id", freshKey).copy(Instance.InstanceStatus.DOWN);

        // when.
        instanceRegistry.reconcile(Map.of(freshKey, updated), Set.of(retainedKey));

        // then.
        assertThat(instanceRegistry.getAll())
                .extracting(it -> it.id().instanceId())
                .containsOnly("fresh-id", "retained-id");
        assertThat(instanceRegistry.get(InstanceId.of("fresh-id")))
                .get()
                .extracting(Instance::status)
                .isEqualTo(Instance.InstanceStatus.DOWN);
    }

    @Test
    void reconcile_shouldKeepSelfRegisteredInstances() {
        // given.
        instanceRegistry.reload(TestInstanceFactory.create("self-registered-id"));

        // when.
        instanceRegistry.reconcile(Map.of(), Set.of());

        // then.
        assertThat(instanceRegistry.getAll())
                .extracting(it -> it.id().instanceId())
                .containsOnly("self-registered-id");
    }

    @Test
    void reconcile_shouldInsertPodVisibleUnderSeveralServicesOnce() {
        // given.
        InstanceKey first = new InstanceKey("service-1", "10.0.0.1", 8080);
        InstanceKey second = new InstanceKey("service-2", "10.0.0.1", 8080);

        // when.
        instanceRegistry.reconcile(
                Map.of(first, discovered("pod-id", first), second, discovered("pod-id", second)), Set.of());

        // then.
        assertThat(instanceRegistry.getAll()).hasSize(1);
    }

    @Test
    void reconcile_shouldNotInsertFreshInstanceWhosePodIsRetainedUnderAnotherService() {
        // given.
        InstanceKey retained = new InstanceKey("service-1", "10.0.0.1", 8080);
        InstanceKey fresh = new InstanceKey("service-2", "10.0.0.1", 8080);
        jdbcAggregateTemplate.insert(discovered("pod-id", retained));

        // when.
        instanceRegistry.reconcile(Map.of(fresh, discovered("pod-id", fresh)), Set.of(retained));

        // then.
        assertThat(instanceRegistry.get(InstanceId.of("pod-id")))
                .get()
                .extracting(Instance::discoveryKey)
                .isEqualTo(retained);
    }

    private static Instance discovered(String id, InstanceKey key) {
        return TestInstanceFactory.create(id, (Instant) null).withDiscoveryKey(key);
    }

    @Test
    void deRegister_shouldRemoveInstance() {
        // given.
        Instance instance = TestInstanceFactory.create("deregister-id-1");
        instanceRegistry.reload(instance);
        assertThat(instanceRegistry.get(InstanceId.of("deregister-id-1"))).isNotEmpty();

        // when.
        instanceRegistry.deRegister(InstanceId.of("deregister-id-1"));

        // then.
        assertThat(instanceRegistry.get(InstanceId.of("deregister-id-1"))).isEmpty();
    }

    @Test
    void deRegisterAll_shouldRemoveAllInstances() {
        // given.
        instanceRegistry.reload(TestInstanceFactory.create("deregister-all-1"));
        instanceRegistry.reload(TestInstanceFactory.create("deregister-all-2"));

        assertThat(instanceRegistry.getAll()).hasSize(2);

        // when.
        instanceRepository.deleteAllById(List.of(InstanceId.of("deregister-all-1"), InstanceId.of("deregister-all-2")));

        // then.
        assertThat(instanceRepository.findAll()).isEmpty();
    }

    @Test
    void getAll_shouldReturnAllInstances() {
        // given.
        instanceRegistry.reload(TestInstanceFactory.create("test-id-1"));
        instanceRegistry.reload(TestInstanceFactory.create("test-id-2"));

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

        instanceRegistry.reload(petclinicInstance);
        instanceRegistry.reload(featureServiceInstance);

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
