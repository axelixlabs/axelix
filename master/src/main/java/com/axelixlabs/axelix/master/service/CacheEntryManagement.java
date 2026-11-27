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

import java.nio.charset.StandardCharsets;
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
 * Previews and evicts entries in a named cache across the selected application instances.
 */
@Service
public class CacheEntryManagement {

    private final EndpointInvoker endpointInvoker;
    private final BulkInstancesValidator bulkInstancesValidator;

    public CacheEntryManagement(EndpointInvoker endpointInvoker, BulkInstancesValidator bulkInstancesValidator) {
        this.endpointInvoker = endpointInvoker;
        this.bulkInstancesValidator = bulkInstancesValidator;
    }

    public Preview preview(List<String> instanceIds, String cacheManagerName, String cacheName, String pattern) {
        validate(instanceIds, cacheManagerName, cacheName, pattern, true);
        HttpPayload payload = new DefaultHttpPayload(
                List.of(new SingleValueQueryParameter("pattern", pattern)),
                Map.of("cacheManagerName", cacheManagerName, "cacheName", cacheName));

        Map<String, Long> estimatedMatches = new LinkedHashMap<>();
        List<String> failedInstanceIds = new ArrayList<>();
        for (String instanceId :
                instanceIds.stream().filter(StringUtils::hasText).distinct().toList()) {
            try {
                byte[] response = endpointInvoker.invoke(
                        InstanceId.of(instanceId), ActuatorEndpoints.PREVIEW_CACHE_ENTRIES, payload);
                estimatedMatches.put(instanceId, Long.parseLong(new String(response, StandardCharsets.UTF_8).strip()));
            } catch (EndpointInvocationException
                    | BadRequestException
                    | InstanceNotFoundException
                    | NumberFormatException e) {
                failedInstanceIds.add(instanceId);
            }
        }
        return new Preview(estimatedMatches, failedInstanceIds);
    }

    public void evict(
            List<String> instanceIds, String cacheManagerName, String cacheName, String selector, boolean wildcard) {
        validate(instanceIds, cacheManagerName, cacheName, selector, wildcard);
        HttpPayload payload = new DefaultHttpPayload(
                List.of(
                        new SingleValueQueryParameter("selector", selector),
                        new SingleValueQueryParameter("wildcard", Boolean.toString(wildcard))),
                Map.of("cacheManagerName", cacheManagerName, "cacheName", cacheName));
        endpointInvoker.invokeNoValueForInstances(instanceIds, ActuatorEndpoints.EVICT_CACHE_ENTRIES, payload);
    }

    private void validate(
            List<String> instanceIds, String cacheManagerName, String cacheName, String selector, boolean wildcard) {
        try {
            BulkRequestValidator.validate(instanceIds, cacheManagerName, cacheName);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(Objects.toString(e.getMessage(), "Invalid cache entry request"));
        }
        if (selector == null || (wildcard && (selector.isBlank() || selector.length() > 256))) {
            throw new BadRequestException("Provide a key or a wildcard pattern of 1 to 256 characters");
        }
        bulkInstancesValidator.validate(instanceIds);
    }

    public record Preview(Map<String, Long> estimatedMatches, List<String> failedInstanceIds) {}
}
