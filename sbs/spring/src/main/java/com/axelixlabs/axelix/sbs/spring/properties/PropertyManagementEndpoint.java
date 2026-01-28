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
package com.axelixlabs.axelix.sbs.spring.properties;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Custom Spring Boot Actuator endpoint
 * that exposes operations for managing application properties at runtime.
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@RestControllerEndpoint(id = "axelix-property-management")
public class PropertyManagementEndpoint {

    private static final Logger log = LoggerFactory.getLogger(PropertyManagementEndpoint.class);

    private final PropertyMutator propertyMutator;

    private final PropertyNameDiscoverer propertyNameDiscoverer;

    public PropertyManagementEndpoint(PropertyMutator propertyMutator, PropertyNameDiscoverer propertyNameDiscoverer) {
        this.propertyMutator = propertyMutator;
        this.propertyNameDiscoverer = propertyNameDiscoverer;
    }

    @PostMapping
    public ResponseEntity<Void> mutate(@RequestBody PropertyMutationRequest request) {
        String propertyName = request.propertyName();

        if (propertyName == null || propertyName.isBlank()) {
            log.warn("Received property mutation request with blank/empty/null property name");
            return ResponseEntity.badRequest().build();
        }

        String discoveredPropertyName = propertyNameDiscoverer.discover(propertyName);

        propertyMutator.mutate(Objects.requireNonNullElse(discoveredPropertyName, propertyName), request.newValue());

        return ResponseEntity.noContent().build();
    }
}
