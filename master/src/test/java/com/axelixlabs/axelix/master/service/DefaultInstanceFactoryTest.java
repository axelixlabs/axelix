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
package com.axelixlabs.axelix.master.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.master.domain.Instance;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link InstanceFactory}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultInstanceFactoryTest {

    private InstanceFactory instanceFactory = new DefaultInstanceFactory();

    @Test
    void createInstance() {
        // when.
        Instance instance = instanceFactory.createInstance(
                "3c994958-924f-4a12-87d0-a8782e97af10",
                "petclinic",
                "2025-02-03T13:29:29Z",
                "http://localhost:8080",
                mapMetadata());

        // then.
        assertThat(instance).isNotNull();
        assertThat(instance.id().instanceId()).isEqualTo("3c994958-924f-4a12-87d0-a8782e97af10");
        assertThat(instance.name()).isEqualTo("petclinic");
        assertThat(instance.serviceVersion()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(instance.javaVersion()).isEqualTo("25");
        assertThat(instance.springBootVersion()).isEqualTo("3.5.0");
        assertThat(instance.springFrameworkVersion()).isEqualTo("6.1.2");
        assertThat(instance.kotlinVersion()).isNull();
        assertThat(instance.jdkVendor()).isEqualTo("BellSoft");
        assertThat(instance.commitShaShort()).isEqualTo("a8b0929");
        assertThat(instance.deployedAt()).isEqualTo("2025-02-03T13:29:29Z");
        assertThat(instance.status()).isEqualTo(Instance.InstanceStatus.UP);
        assertThat(instance.memoryUsage().heap()).isEqualTo(12000.0);
        assertThat(instance.actuatorUrl()).isEqualTo("http://localhost:8080/actuator");
        assertThat(instance.vmFeatures()).hasSize(1).first().satisfies(vmFeature -> {
            assertThat(vmFeature.name()).isEqualTo("AppCDS");
            assertThat(vmFeature.description()).isEqualTo("AppCDS Description");
            assertThat(vmFeature.enabled()).isFalse();
        });
    }

    private BasicDiscoveryMetadata mapMetadata() {
        BasicDiscoveryMetadata.SoftwareVersions softwareVersions =
                new BasicDiscoveryMetadata.SoftwareVersions("25", "3.5.0", "6.1.2", null);

        BasicDiscoveryMetadata.MemoryDetails memoryDetails = new BasicDiscoveryMetadata.MemoryDetails(12_000);

        BasicDiscoveryMetadata.VMFeature vmFeature =
                new BasicDiscoveryMetadata.VMFeature("AppCDS", "AppCDS Description", false);

        return new BasicDiscoveryMetadata(
                "1.0.0-SNAPSHOT",
                "3.5.0-SNAPSHOT",
                "a8b0929",
                "BellSoft",
                softwareVersions,
                BasicDiscoveryMetadata.HealthStatus.UP,
                memoryDetails,
                List.of(vmFeature));
    }
}
