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

/**
 * Central registry of all the {@link Instance instances} that this Master deployment is aware about.
 * It is guaranteed that all the instances inside this registry have the unique instance id. The implementations
 * must be thread safe.
 *
 * @see Instance
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@NullMarked
public interface InstanceRegistry {

    /**
     * Registers the given instance inside the registry. In case the {@link Instance} with this ID
     * is already present, it will be updated with the new data (upsert semantics).
     *
     * @param instance the instance to be registered or updated
     */
    void register(Instance instance);

    /**
     * Registers all provided {@linkplain Instance Instances} within the registry.
     *
     * @param instances the instances that need to be registered
     */
    void registerAll(Collection<Instance> instances);

    /**
     * Reloads the registry.
     *
     * It essentially means that the entire database of Instances is wiped out, and after that
     * only the provided Instances are persisted. So the passed collection of instances is effectively
     * the new state of this Instance Registry
     *
     * @param instances the instances to replace all the existing onces
     */
    void reload(Collection<Instance> instances);

    /**
     * Deregisters the {@link Instance} by the instanceId.
     *
     * @param instanceId the id of the instance that is supposed to be deregistered.
     */
    void deRegister(InstanceId instanceId);

    /**
     * Deregisters all instances identified by the given instance IDs.
     *
     * @param instanceIds the collection of IDs of the instances to be deregistered.
     */
    void deRegisterAll(Collection<InstanceId> instanceIds);

    /**
     * Get {@link Instance} by its id.
     *
     * @param instanceId the id of the instance to get.
     * @return Optional wrapping an {@link Instance} that is identified by
     *         given {@code instanceId} an empty {@link Optional} otherwise.
     */
    Optional<Instance> get(InstanceId instanceId);

    /**
     * Get all instances that are managed by this registry.
     *
     * @return all instances that are managed by this registry.
     */
    List<Instance> getAll();

    /**
     * Get all instance IDs that are managed by this registry.
     *
     * @return all instance IDs that are managed by this registry.
     */
    Set<InstanceId> getAllIds();

    /**
     * @return average heap usage in bytes across all instances, or -1 if no instances registered.
     */
    double getAverageHeapSize();

    /**
     * @return total heap usage in bytes across all instances, or 0 if no instances registered.
     */
    double getTotalHeapSize();

    /**
     * Find instance by the arbitrary search query
     *
     * @return {@link Instance}
     */
    Set<Instance> findByQuery(String query);
}
