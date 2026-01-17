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
package com.nucleonforge.axelix.master.service.state;

import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axelix.master.exception.InstanceAlreadyRegisteredException;
import com.nucleonforge.axelix.master.exception.InstanceNotFoundException;
import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.model.instance.InstanceId;

/**
 * Central registry of all the {@link Instance instances} that this Master deployment is aware about.
 * It is guaranteed that all the instances inside this registry have the unique instance id. The implementations
 * must be thread safe.
 *
 * @see Instance
 * @author Mikhail Polivakha
 */
public interface InstanceRegistry {

    /**
     * Register the given instance inside the registry. In case the {@link Instance} with this ID
     * is already present then re-registration must not happen and the exception must be thrown.
     *
     * @param instance the instance to be registered
     * @throws InstanceAlreadyRegisteredException in case the {@link Instance} with
     *         the same id is already present in the registry
     */
    void register(Instance instance) throws InstanceAlreadyRegisteredException;

    /**
     * Deregister the {@link Instance} by the instanceId.
     *
     * @param instanceId the id of the instance that is supposed to be deregistered.
     * @throws InstanceNotFoundException in case such an {@link Instance} is not found.
     */
    void deRegister(InstanceId instanceId) throws InstanceNotFoundException;

    /**
     * Deregister and register the {@link Instance}. If the {@link Instance} with such {@link InstanceId}
     * is not present in the registry, then simply new {@link Instance} is registered.
     *
     * @param  instance the instance to be registered
     */
    void replace(Instance instance);

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
    @NonNull
    Set<Instance> getAll();
}
