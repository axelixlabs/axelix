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
package com.axelixlabs.axelix.master.api.external.endpoint.caches;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.caches.CacheBulkClearRequest;
import com.axelixlabs.axelix.master.api.external.request.caches.CachesBulkClearRequest;
import com.axelixlabs.axelix.master.service.BulkRequestValidator;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;
import com.axelixlabs.axelix.master.service.transport.PartiallyUpdatedException;

/**
 * The API for clearing caches across several instances.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@ExternalApiRestController
public class CachesClearApi {

    private final EndpointInvoker endpointInvoker;

    public CachesClearApi(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @DeleteMapping(ApiPaths.CachesApi.CACHE_BULK_CLEAR_ALL)
    public ResponseEntity<?> clearAllCaches(@RequestBody CachesBulkClearRequest request) {
        if (BulkRequestValidator.hasMissingRequiredFields(request.instanceIds())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            endpointInvoker.invokeNoValueForInstances(
                    request.instanceIds(), ActuatorEndpoints.CLEAR_ALL_CACHES, NoHttpPayload.INSTANCE);
            return ResponseEntity.noContent().build();
        } catch (PartiallyUpdatedException e) {
            return ResponseEntity.badRequest()
                    .body(new SimpleApiError(
                            ApiErrorCodes.PARTIALLY_UPDATED.getErrorCode(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @DeleteMapping(ApiPaths.CachesApi.CACHE_BULK_CLEAR_CACHE)
    public ResponseEntity<?> clearSpecificCacheEntity(@RequestBody CacheBulkClearRequest request) {
        if (BulkRequestValidator.hasMissingRequiredFields(
                request.instanceIds(), request.cacheManagerName(), request.cacheName())) {
            return ResponseEntity.badRequest().build();
        }

        HttpPayload payload = new DefaultHttpPayload(
                Map.of("cacheName", request.cacheName(), "cacheManagerName", request.cacheManagerName()));

        try {
            endpointInvoker.invokeNoValueForInstances(
                    request.instanceIds(), ActuatorEndpoints.CLEAR_SINGLE_CACHE, payload);
            return ResponseEntity.noContent().build();
        } catch (PartiallyUpdatedException e) {
            return ResponseEntity.badRequest()
                    .body(new SimpleApiError(
                            ApiErrorCodes.PARTIALLY_UPDATED.getErrorCode(), HttpStatus.BAD_REQUEST.value()));
        }
    }
}
