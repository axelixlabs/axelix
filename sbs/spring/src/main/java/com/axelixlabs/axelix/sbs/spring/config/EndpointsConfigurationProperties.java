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
package com.axelixlabs.axelix.sbs.spring.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties that apply across different endpoints.
 *
 * @author Mikhail Polivakha
 */
@ConfigurationProperties(prefix = "axelix.sbs.endpoints.config")
public class EndpointsConfigurationProperties {

    public static final List<String> SANITIZE_ALL = List.of("*");

    /**
     * List of properties whose values needs to be sanitized before being returned.
     * Single value of {@code "*"} means all properties must be sanitized.
     */
    private List<String> sanitizedProperties = List.of();

    public List<String> getSanitizedProperties() {
        return sanitizedProperties;
    }

    public EndpointsConfigurationProperties setSanitizedProperties(List<String> sanitizedProperties) {
        this.sanitizedProperties = sanitizedProperties;
        return this;
    }
}
