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
package com.axelixlabs.axelix.master.repository.entity;

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import com.axelixlabs.axelix.master.domain.Instance;

/**
 * Persistence entity representing a registered service instance.
 *
 * @see Instance
 * @author Nikita Kirillov
 */
@Table("instances")
public record InstanceEntity(
        @Id String id,
        String name,
        String serviceVersion,
        String javaVersion,
        String springBootVersion,
        String springFrameworkVersion,
        @Nullable String kotlinVersion,
        String jdkVendor,
        String commitShaShort,
        @Nullable String deployedAt,
        String status,
        double heap,
        String actuatorUrl,

        @MappedCollection(idColumn = "instance_id", keyColumn = "instances_key")
        List<VmFeatureEntity> vmFeatures) {}
