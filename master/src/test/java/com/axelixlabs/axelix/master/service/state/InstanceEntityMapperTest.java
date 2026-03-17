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

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.Instance.VMFeature;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;
import com.axelixlabs.axelix.master.repository.entity.InstanceEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link InstanceEntityMapper}
 *
 * @author Nikita Kirillov
 */
class InstanceEntityMapperTest {

    private final InstanceEntityMapper mapper = new InstanceEntityMapper();

    private static Instance createInstance(Instant deployedAt) {
        return new Instance(
                InstanceId.of("test-id"),
                "name",
                "1.0.0",
                "java-17",
                "SB-3",
                "Spring-6",
                "2.0.0",
                "BellSoft",
                "sha",
                deployedAt,
                Instance.InstanceStatus.UP,
                new MemoryUsage(1234d),
                "http://localhost:8080/actuator",
                List.of(new VMFeature("feature-1", "desc-1", true), new VMFeature("feature-2", "desc-2", false)));
    }

    @Test
    void toEntity_shouldMapAllFields() {
        Instant now = Instant.now();
        Instance instance = createInstance(now);

        InstanceEntity entity = mapper.toEntity(instance);

        assertThat(entity.id()).isEqualTo("test-id");
        assertThat(entity.name()).isEqualTo("name");
        assertThat(entity.serviceVersion()).isEqualTo("1.0.0");
        assertThat(entity.javaVersion()).isEqualTo("java-17");
        assertThat(entity.springBootVersion()).isEqualTo("SB-3");
        assertThat(entity.springFrameworkVersion()).isEqualTo("Spring-6");
        assertThat(entity.kotlinVersion()).isEqualTo("2.0.0");
        assertThat(entity.jdkVendor()).isEqualTo("BellSoft");
        assertThat(entity.commitShaShort()).isEqualTo("sha");
        assertThat(entity.deployedAt()).isEqualTo(now.toString());
        assertThat(entity.status()).isEqualTo("UP");
        assertThat(entity.heap()).isEqualTo(1234d);
        assertThat(entity.actuatorUrl()).isEqualTo("http://localhost:8080/actuator");
        assertThat(entity.vmFeatures()).hasSize(2);
        assertThat(entity.vmFeatures().get(0).enabled()).isEqualTo(1);
        assertThat(entity.vmFeatures().get(1).enabled()).isEqualTo(0);
    }

    @Test
    void toDomain_shouldMapAllFields() {
        Instant instant = Instant.now();
        InstanceEntity entity = mapper.toEntity(createInstance(instant));

        Instance domain = mapper.toDomain(entity);

        assertThat(domain.id()).isEqualTo(InstanceId.of("test-id"));
        assertThat(domain.name()).isEqualTo("name");
        assertThat(domain.serviceVersion()).isEqualTo("1.0.0");
        assertThat(domain.javaVersion()).isEqualTo("java-17");
        assertThat(domain.springBootVersion()).isEqualTo("SB-3");
        assertThat(domain.springFrameworkVersion()).isEqualTo("Spring-6");
        assertThat(domain.kotlinVersion()).isEqualTo("2.0.0");
        assertThat(domain.jdkVendor()).isEqualTo("BellSoft");
        assertThat(domain.commitShaShort()).isEqualTo("sha");
        assertThat(domain.status()).isEqualTo(Instance.InstanceStatus.UP);
        assertThat(domain.memoryUsage().heap()).isEqualTo(1234d);
        assertThat(domain.actuatorUrl()).isEqualTo("http://localhost:8080/actuator");
        assertThat(domain.vmFeatures()).hasSize(2);
        assertThat(domain.vmFeatures().get(0).enabled()).isTrue();
        assertThat(domain.vmFeatures().get(1).enabled()).isFalse();
    }

    @Test
    void roundTrip_shouldReturnEqualInstance() {
        Instant instant = Instant.now();
        Instance original = createInstance(instant);

        Instance result = mapper.toDomain(mapper.toEntity(original));

        assertThat(result).isEqualTo(original);
    }
}
