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
package com.axelixlabs.axelix.master.api.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axelixlabs.axelix.common.api.registration.SelfRegistrationMetadata;
import com.axelixlabs.axelix.master.service.discovery.selfregistered.SelfRegisteredServiceDiscoverer;

/**
 * The API used for service self-registration.
 *
 * @author Sergey Cherkasov
 */
@RestController
@RequestMapping(path = InternalApiPaths.SelfRegistryApi.MAIN)
@ConditionalOnBean(SelfRegisteredServiceDiscoverer.class)
public class SelfRegisteredApi {

    private final SelfRegisteredServiceDiscoverer selfRegisteredServiceDiscoverer;

    public SelfRegisteredApi(SelfRegisteredServiceDiscoverer selfRegisteredServiceDiscoverer) {
        this.selfRegisteredServiceDiscoverer = selfRegisteredServiceDiscoverer;
    }

    @PostMapping(path = InternalApiPaths.SelfRegistryApi.SERVICE_REGISTER)
    public ResponseEntity<Void> registryServiceInstance(@RequestBody SelfRegistrationMetadata request) {
        selfRegisteredServiceDiscoverer.registerNewInstances(request);
        return ResponseEntity.noContent().build();
    }
}
