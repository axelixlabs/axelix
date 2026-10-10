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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.ProbeState.InstanceKey;

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
     * Reloads the given instance inside the registry. In case the {@link Instance} with this ID
     * is already present, it will be updated with the new data (upsert semantics), if case it is
     * not present - it is going to be created.
     *
     * @param instance the instance to be registered or updated
     */
    void reload(Instance instance);

    /**
     * Reconciles the auto-discovered Instances with the result of the latest discovery run. A previously
     * auto-discovered Instance is replaced if it is present in {@code fresh}, kept as is if it is present in
     * {@code retained}, and removed otherwise. The Instances that have self-registered are not affected by this call.
     *
     * @param fresh    Instances probed successfully during the run, by their discovery key
     * @param retained discovery keys of the Instances that must be kept as they are, since they were not probed
     *                 successfully during the run, but are still considered alive
     */
    void reconcile(Map<InstanceKey, Instance> fresh, Set<InstanceKey> retained);

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
     * Get all instances that are managed by this registry.
     *
     * @return all instances that are managed by this registry.
     */
    List<Instance> getAll();

    /**
     * Find instance by the arbitrary search query
     *
     * @return {@link Instance}
     */
    Set<Instance> findByQuery(String query);
}
