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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;

import static com.axelixlabs.axelix.master.domain.Instance.InstanceStatus;

/**
 * Central registry of all the {@link Instance instances} that this Master deployment is aware about.
 * It is guaranteed that all the instances inside this registry have the unique instance id. The implementations
 * must be thread safe.
 *
 * @see Instance
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@NullMarked
public interface InstanceRegistry {

    /**
     * Registers the given instance inside the registry. In case the {@link Instance} with this ID
     * is already present, it will be updated with the new data (upsert semantics), if case it is
     * not present - it is going to be created.
     *
     * @param instance the instance to be registered or updated
     */
    void register(Instance instance);

    /**
     * Refreshes the status and the memory usage of the given {@link Instance}.
     */
    void refresh(InstanceId instanceId, InstanceStatus status, MemoryUsage memoryUsage);

    /**
     * Reloads the registry, by removing all previously auto-discovered Instances,
     * and inserts the following discovered instances instead. The Instances that have
     * self-registered are not removed by this call.
     *
     * @param instances Instances to register as a replacement of previously auto-discovered instances.
     */
    void register(Collection<Instance> instances);

    /**
     * Deregisters the {@link Instance} by the instanceId.
     *
     * @param instanceId the id of the instance that is supposed to be deregistered.
     */
    void deRegister(InstanceId instanceId);

    /**
     * Get {@link Instance} by its id.
     *
     * @param instanceId the id of the instance to get.
     * @return Optional wrapping an {@link Instance} that is identified by
     *         given {@code instanceId} an empty {@link Optional} otherwise.
     */
    Optional<Instance> get(InstanceId instanceId);

    /**
     * Get {@link Instance} by its id, failing if there is no such instance.
     *
     * @param instanceId the id of the instance to get.
     * @return the {@link Instance} that is identified by given {@code instanceId}.
     * @throws InstanceNotFoundException if there is no instance with the given id.
     */
    default Instance getOrThrow(InstanceId instanceId) throws InstanceNotFoundException {
        return get(instanceId).orElseThrow(() -> new InstanceNotFoundException(instanceId));
    }

    /**
     * Get all instances that are managed by this registry.
     *
     * @return all instances that are managed by this registry.
     */
    List<Instance> getAll();

    /**
     * Loads only the instances with the given IDs. Missing instances are omitted.
     */
    List<Instance> getAllByIds(Collection<InstanceId> instanceIds);

    /**
     * Find instance by the arbitrary search query
     *
     * @return {@link Instance}
     */
    Set<Instance> findByQuery(String query);

    /**
     * Get all instances that belong to the same application as the given instance, including the instance itself.
     *
     * @param instanceId the id of the instance whose siblings to get.
     * @return all instances of the given instance's application, sorted by name ignoring case.
     * @throws InstanceNotFoundException if there is no instance with the given id.
     */
    List<Instance> findSiblings(InstanceId instanceId) throws InstanceNotFoundException;
}
