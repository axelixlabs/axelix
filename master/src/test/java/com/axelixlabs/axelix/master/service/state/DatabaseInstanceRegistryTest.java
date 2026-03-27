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
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.Instance.VMFeature;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;

import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static com.axelixlabs.axelix.master.utils.TestObjectFactory.withName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DatabaseInstanceRegistry}
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest
class DatabaseInstanceRegistryTest {

    @Autowired
    private InstanceRegistry instanceRegistry;

    @BeforeEach
    void setup() {
        instanceRegistry.deRegisterAll(instanceRegistry.getAllIds());
    }

    @AfterEach
    void cleanup() {
        instanceRegistry.deRegisterAll(instanceRegistry.getAllIds());
    }

    @Test
    void register_shouldPersistInstance() {
        Instant instant = Instant.now();
        Instance instance = new Instance(
                InstanceId.of("test-id-1"),
                "name",
                "1.0.0",
                "java-17",
                "SB-3",
                "Spring-6",
                "2.0.0",
                "BellSoft",
                "sha-commit",
                instant,
                Instance.InstanceStatus.UP,
                new MemoryUsage(1234d),
                "Http://localhost:8080/actuator",
                Set.of(
                        new VMFeature("feature-1", "description-1", true),
                        new VMFeature("feature-2", "description-2", false)));

        instanceRegistry.register(instance);

        Optional<Instance> expectedInstance = instanceRegistry.get(InstanceId.of("test-id-1"));
        assertThat(expectedInstance).isPresent();
        assertThat(expectedInstance.get()).isEqualTo(instance);
    }

    @Test
    void register_shouldUpdateExistingInstance() {
        Instant instant = Instant.now();
        Instance instance = new Instance(
                InstanceId.of("test-id-2"),
                "name",
                "1.0.0",
                "java-17",
                "SB-3",
                "Spring-6",
                "2.0.0",
                " BellSoft",
                "sha",
                instant,
                Instance.InstanceStatus.UP,
                new MemoryUsage(1234d),
                "Http://localhost:8080/actuator",
                Set.of(
                        new VMFeature("feature-1", "description-1", true),
                        new VMFeature("feature-2", "description-2", false)));
        instanceRegistry.register(instance);

        Instance updated = new Instance(
                instance.id(),
                "updated-name",
                "1.0.1",
                "java-21",
                "SB-4",
                "Spring-7",
                "2.2.0",
                "Axiom JDK",
                "new-sha",
                Instant.now(),
                Instance.InstanceStatus.DOWN,
                new MemoryUsage(1200d),
                instance.actuatorUrl(),
                instance.vmFeatures());
        instanceRegistry.register(updated);

        Optional<Instance> found = instanceRegistry.get(InstanceId.of("test-id-2"));
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(updated);
    }

    @Test
    void registerAll_shouldPersistAllInstances() {
        List<Instance> instances =
                List.of(createInstance("batch-id-1"), createInstance("batch-id-2"), createInstance("batch-id-3"));

        for (int i = 0; i < 3; i++) {
            instanceRegistry.registerAll(instances);
        }

        assertThat(instanceRegistry.getAll()).hasSize(3);
    }

    @Test
    void deRegister_shouldRemoveInstance() {
        Instance instance = createInstance("deregister-id-1");

        instanceRegistry.register(instance);
        assertThat(instanceRegistry.get(InstanceId.of("deregister-id-1"))).isNotEmpty();

        instanceRegistry.deRegister(InstanceId.of("deregister-id-1"));
        assertThat(instanceRegistry.get(InstanceId.of("deregister-id-1"))).isEmpty();
    }

    @Test
    void deRegisterAll_shouldRemoveAllInstances() {
        instanceRegistry.register(createInstance("deregister-all-1"));
        instanceRegistry.register(createInstance("deregister-all-2"));

        assertThat(instanceRegistry.getAll()).hasSize(2);

        instanceRegistry.deRegisterAll(List.of(InstanceId.of("deregister-all-1"), InstanceId.of("deregister-all-2")));

        assertThat(instanceRegistry.getAllIds())
                .doesNotContain(InstanceId.of("deregister-all-1"), InstanceId.of("deregister-all-2"));
    }

    @Test
    void getAll_shouldReturnAllInstances() {
        instanceRegistry.register(createInstance("test-id-1"));
        instanceRegistry.register(createInstance("test-id-2"));

        assertThat(instanceRegistry.getAll())
                .extracting(Instance::id)
                .containsOnly(InstanceId.of("test-id-1"), InstanceId.of("test-id-2"));
    }

    @Test
    void getAllIds_shouldReturnOnlyIds() {
        instanceRegistry.register(createInstance("test-id-1"));
        instanceRegistry.register(createInstance("test-id-2"));

        Set<InstanceId> ids = instanceRegistry.getAllIds();
        assertThat(ids).containsOnly(InstanceId.of("test-id-1"), InstanceId.of("test-id-2"));
    }

    @Test
    void getAverageHeapSize_shouldReturnAverage() {
        instanceRegistry.register(createInstanceWithHeap("heap-id-1", 100.0));
        instanceRegistry.register(createInstanceWithHeap("heap-id-2", 200.0));

        assertThat(instanceRegistry.getAverageHeapSize()).isEqualTo(150.0);
    }

    @Test
    void getTotalHeapSize_shouldReturnSum() {
        instanceRegistry.register(createInstanceWithHeap("total-id-1", 100.0));
        instanceRegistry.register(createInstanceWithHeap("total-id-2", 200.0));

        assertThat(instanceRegistry.getTotalHeapSize()).isEqualTo(300.0);
    }

    @Test
    void findByQuery_shouldReturnMatchingInstances() {
        Instance petclinicInstance = withName("query-id-1", "petclinic-service");
        Instance featureServiceInstance = withName("query-id-2", "feature-service");

        instanceRegistry.register(petclinicInstance);
        instanceRegistry.register(featureServiceInstance);

        Set<Instance> result = instanceRegistry.findByQuery("petclinic");
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next()).isEqualTo(petclinicInstance);
    }

    private Instance createInstanceWithHeap(String instanceId, double heap) {
        return new Instance(
                InstanceId.of(instanceId),
                "updated-name",
                "1.0.1",
                "java-21",
                "SB-4",
                "Spring-7",
                "2.2.0",
                "Axiom JDK",
                "new-sha",
                Instant.now(),
                Instance.InstanceStatus.DOWN,
                new MemoryUsage(heap),
                "/actuator",
                Set.of());
    }
}
