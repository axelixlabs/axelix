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

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;

/**
 * Component that is responsible for the Instance registration/de-registration process.
 * <p>
 * The implementation of this class serve as the entrypoint for the {@link Instance} registration
 * or deregistration process. In other words, when calling either register/de-register methods
 * on this API, the implementation guarantee that the passed {@link Instance} is fully and completely
 * reigstered/de-registered inside/from Master.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public interface InstanceRegistrar {

    /**
     * Register the given {@link Instance} inside master.
     *
     * @param instance the {@link Instance} to be registered
     */
    void register(Instance instance);

    /**
     * Clear the necessary state associated with {@link Instance} with the given {@link InstanceId}.
     *
     * @param instanceId the id of the {@link Instance} to deregister.
     */
    void deregister(InstanceId instanceId);
}
