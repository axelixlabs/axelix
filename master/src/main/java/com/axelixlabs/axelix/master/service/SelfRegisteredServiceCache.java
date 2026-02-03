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

import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.api.selfregistered.SelfRegisteredServiceRequest;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.internal.SelfRegisteredApi;

/**
 * Stores services that have sent a self-registration request via {@link SelfRegisteredApi}.
 *
 * @author Sergey Cherkasov
 */
@NullMarked
public interface SelfRegisteredServiceCache {

    /**
     * Gets the self-registered service identified by the given {@link InstanceId}.
     *
     * @param instanceId the id of the self-registered {@link Instance} to retrieve.
     * @return Optional wrapping an {@link SelfRegisteredServiceRequest} that is identified by
     *         given {@code instanceId} an empty {@link Optional} otherwise.
     */
    Optional<SelfRegisteredServiceRequest> getService(InstanceId instanceId);

    /**
     * Record the service that sent the self-registration request
     *
     * @param service the service that initiated the self-registration request.
     */
    void putService(SelfRegisteredServiceRequest service);

    /**
     * Clear the record of the service with the given {@link InstanceId}.
     *
     * @param instanceId the id of the self-registered {@link Instance} to remove.
     */
    void clearService(InstanceId instanceId);

    /**
     * Get all services.
     *
     * @return all services that have sent a self-registration request.
     */
    Set<SelfRegisteredServiceRequest> getAllServices();
}
