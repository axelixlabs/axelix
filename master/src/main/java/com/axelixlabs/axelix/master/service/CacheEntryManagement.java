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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.http.SingleValueQueryParameter;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * Checks and evicts entries in a named cache across the selected application instances.
 */
@Service
public class CacheEntryManagement {

    private final EndpointInvoker endpointInvoker;
    private final BulkInstancesValidator bulkInstancesValidator;

    public CacheEntryManagement(EndpointInvoker endpointInvoker, BulkInstancesValidator bulkInstancesValidator) {
        this.endpointInvoker = endpointInvoker;
        this.bulkInstancesValidator = bulkInstancesValidator;
    }

    public KeyPresence exists(List<String> instanceIds, String cacheManagerName, String cacheName, String key) {
        HttpPayload payload = entryPayload(instanceIds, cacheManagerName, cacheName, key);
        Map<String, Boolean> existsByInstance = new LinkedHashMap<>();
        List<String> failedInstanceIds = new ArrayList<>();
        for (String instanceId :
                instanceIds.stream().filter(StringUtils::hasText).distinct().toList()) {
            try {
                Boolean exists =
                        endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.CACHE_KEY_EXISTS, payload);
                existsByInstance.put(instanceId, exists);
            } catch (EndpointInvocationException | BadRequestException | InstanceNotFoundException e) {
                failedInstanceIds.add(instanceId);
            }
        }
        return new KeyPresence(existsByInstance, failedInstanceIds);
    }

    public void evict(List<String> instanceIds, String cacheManagerName, String cacheName, String key) {
        HttpPayload payload = entryPayload(instanceIds, cacheManagerName, cacheName, key);
        endpointInvoker.invokeNoValueForInstances(instanceIds, ActuatorEndpoints.EVICT_CACHE_ENTRY, payload);
    }

    private HttpPayload entryPayload(List<String> instanceIds, String cacheManagerName, String cacheName, String key) {
        try {
            BulkRequestValidator.validate(instanceIds, cacheManagerName, cacheName);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(Objects.toString(e.getMessage(), "Invalid cache entry request"));
        }
        if (key == null) {
            throw new BadRequestException("Provide a cache key");
        }
        bulkInstancesValidator.validate(instanceIds);
        return new DefaultHttpPayload(
                List.of(new SingleValueQueryParameter("key", key)),
                Map.of("cacheManagerName", cacheManagerName, "cacheName", cacheName));
    }

    public record KeyPresence(Map<String, Boolean> existsByInstance, List<String> failedInstanceIds) {}
}
