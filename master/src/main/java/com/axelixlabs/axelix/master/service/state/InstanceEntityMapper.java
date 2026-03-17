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

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.Instance.VMFeature;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;
import com.axelixlabs.axelix.master.repository.entity.InstanceEntity;
import com.axelixlabs.axelix.master.repository.entity.VmFeatureEntity;

/**
 * Maps between {@link Instance} domain objects and {@link InstanceEntity} persistence entities.
 *
 * @author Nikita Kirillov
 */
@Component
public class InstanceEntityMapper {

    public InstanceEntity toEntity(Instance instance) {
        String instanceId = instance.id().instanceId();
        String deployedAt =
                instance.deployedAt() != null ? instance.deployedAt().toString() : null;

        List<VmFeatureEntity> vmFeatures = instance.vmFeatures().stream()
                .map(f -> new VmFeatureEntity(instanceId, f.name(), f.description(), f.enabled() ? 1 : 0))
                .toList();

        return new InstanceEntity(
                instanceId,
                instance.name(),
                instance.serviceVersion(),
                instance.javaVersion(),
                instance.springBootVersion(),
                instance.springFrameworkVersion(),
                instance.kotlinVersion(),
                instance.jdkVendor(),
                instance.commitShaShort(),
                deployedAt,
                instance.status().name(),
                instance.memoryUsage().heap(),
                instance.actuatorUrl(),
                vmFeatures);
    }

    public Instance toDomain(InstanceEntity entity) {
        Instant deployedAt = entity.deployedAt() != null ? Instant.parse(entity.deployedAt()) : null;

        List<VMFeature> vmFeatures = entity.vmFeatures().stream()
                .map(f -> new VMFeature(f.name(), f.description(), f.enabled() == 1))
                .toList();

        return new Instance(
                InstanceId.of(entity.id()),
                entity.name(),
                entity.serviceVersion(),
                entity.javaVersion(),
                entity.springBootVersion(),
                entity.springFrameworkVersion(),
                entity.kotlinVersion(),
                entity.jdkVendor(),
                entity.commitShaShort(),
                deployedAt,
                Instance.InstanceStatus.valueOf(entity.status()),
                new MemoryUsage(entity.heap()),
                entity.actuatorUrl(),
                vmFeatures);
    }
}
