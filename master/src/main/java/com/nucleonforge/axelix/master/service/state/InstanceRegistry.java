/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
