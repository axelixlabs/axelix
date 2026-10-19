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
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;

/**
 * Resolves the target instances of a bulk operation within one application.
 *
 * @author Sergey Cherkasov
 */
@Service
public class BulkInstancesResolver {

    private final InstanceRegistry instanceRegistry;

    public BulkInstancesResolver(InstanceRegistry instanceRegistry) {
        this.instanceRegistry = instanceRegistry;
    }

    /**
     * Loads the selected instances and checks that they belong to one application.
     * Blank and duplicated IDs are ignored.
     *
     * @param instanceIds the IDs of the selected instances
     * @return the selected instances
     * @throws InstanceNotFoundException if any requested instance is missing
     * @throws BadRequestException if the instances belong to different applications
     */
    public List<Instance> resolve(List<String> instanceIds) {
        List<InstanceId> ids = instanceIds.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .map(InstanceId::of)
                .toList();

        List<Instance> instances = instanceRegistry.getAllByIds(ids);

        if (instances.size() != ids.size()) {
            Set<InstanceId> foundIds = instances.stream().map(Instance::id).collect(Collectors.toSet());
            List<InstanceId> missingIds =
                    ids.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new InstanceNotFoundException(missingIds);
        }

        if (instances.stream().map(Instance::applicationId).distinct().count() > 1) {
            throw new BadRequestException("All instances must belong to the same application");
        }

        return instances;
    }
}
