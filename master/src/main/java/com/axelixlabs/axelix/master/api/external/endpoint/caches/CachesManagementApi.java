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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.caches.CacheBulkStatusChangeRequest;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;
import com.axelixlabs.axelix.master.service.transport.PartiallyUpdatedException;

/**
 * The API for enabling and disabling caches across several instances.
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@ExternalApiRestController
public class CachesManagementApi {

    private final EndpointInvoker endpointInvoker;

    public CachesManagementApi(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @PostMapping(ApiPaths.CachesApi.BULK_ENABLE_CACHE)
    public ResponseEntity<?> enableCache(@RequestBody CacheBulkStatusChangeRequest request) {
        HttpPayload payload = new DefaultHttpPayload(
                Map.of("cacheName", request.cacheName(), "cacheManagerName", request.cacheManagerName()));

        try {
            endpointInvoker.invokeNoValueForInstances(request.instanceIds(), ActuatorEndpoints.ENABLE_CACHE, payload);
            return ResponseEntity.noContent().build();
        } catch (PartiallyUpdatedException e) {
            // TODO: A partial update is generally not the client's fault, so 400 Bad Request is not
            //  really the right status here. We keep it for now since it is a minor issue that should
            //  rarely happen in practice, but this should be revisited in the future.
            return ResponseEntity.badRequest()
                    .body(new SimpleApiError(
                            ApiErrorCodes.PARTIALLY_UPDATED.getErrorCode(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PostMapping(ApiPaths.CachesApi.BULK_DISABLE_CACHE)
    public ResponseEntity<?> disableCache(@RequestBody CacheBulkStatusChangeRequest request) {
        HttpPayload payload = new DefaultHttpPayload(
                Map.of("cacheName", request.cacheName(), "cacheManagerName", request.cacheManagerName()));

        try {
            endpointInvoker.invokeNoValueForInstances(request.instanceIds(), ActuatorEndpoints.DISABLE_CACHE, payload);
            return ResponseEntity.noContent().build();
        } catch (PartiallyUpdatedException e) {
            // TODO: See the note on the 400 Bad Request status in enableCache.
            return ResponseEntity.badRequest()
                    .body(new SimpleApiError(
                            ApiErrorCodes.PARTIALLY_UPDATED.getErrorCode(), HttpStatus.BAD_REQUEST.value()));
        }
    }
}
