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
package com.axelixlabs.axelix.common.api.env;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to env/property/{propertyName} actuator endpoint.
 *
 * @param property        The resolved property with its value and source.
 * @param activeProfiles  The currently active Spring Boot application profiles, not specific to this property.
 * @param defaultProfiles The default Spring Boot application profiles, not specific to this property.
 * @param propertySources The property sources that contributed to resolving this property.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/env.html">Env Endpoint</a>
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
public record EnvironmentProperty(
        @JsonProperty("property") Property property,
        @JsonProperty("activeProfiles") List<String> activeProfiles,
        @JsonProperty("defaultProfiles") List<String> defaultProfiles,
        @JsonProperty("propertySources") List<SourceEntry> propertySources) {

    public record Property(String source, String value) {}

    public record SourceEntry(
            @JsonProperty("name") String sourceName, @JsonProperty("property") @Nullable PropertyValue property) {}
}
