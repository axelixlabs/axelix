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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;

/**
 * Custom Actuator endpoint exposing the application's {@code @ConfigurationProperties}
 * data from the standard Spring Boot Actuator endpoint.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@RestControllerEndpoint(id = "axelix-configprops")
public class AxelixConfigurationPropertiesEndpoint {

    private static final Logger log = LoggerFactory.getLogger(AxelixConfigurationPropertiesEndpoint.class);

    private final ConfigurationPropertiesCache configurationPropertiesCache;

    private final ConfigurationPropertiesMutator configurationPropertiesMutator;

    public AxelixConfigurationPropertiesEndpoint(
            ConfigurationPropertiesCache cache, ConfigurationPropertiesMutator configurationPropertiesMutator) {
        this.configurationPropertiesCache = cache;
        this.configurationPropertiesMutator = configurationPropertiesMutator;
    }

    @GetMapping
    public ConfigurationPropertiesFeed getConfigurationProperties() {
        return configurationPropertiesCache.getConfigProps();
    }

    @PostMapping
    public ResponseEntity<Void> mutateConfigurationProperties(
            @RequestBody ConfigurationPropertyMutationRequest request) {
        String propertyName = request.propertyName();

        if (propertyName == null || propertyName.isBlank()) {
            log.warn("Received ConfigurationProperty mutation request with blank/empty/null property name");
            return ResponseEntity.badRequest().build();
        }

        if (request.newValue() == null) {
            log.warn(
                    "Received ConfigurationProperty mutation request for property '{}' with null newValue",
                    propertyName);
            return ResponseEntity.badRequest().build();
        }

        try {
            configurationPropertiesMutator.mutate(propertyName, request.newValue());
            return ResponseEntity.noContent().build();
        } catch (ConfigurationPropertyMutationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
