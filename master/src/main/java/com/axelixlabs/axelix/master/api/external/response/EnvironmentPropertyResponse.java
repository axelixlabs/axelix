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
package com.axelixlabs.axelix.master.api.external.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.env.EnvironmentProperty;

/**
 * The response object that represents an environment property in the application.
 *
 * @param source the name of the property source from which this value was obtained
 * @param value the string representation of the property's value
 *
 * @see EnvironmentProperty
 * @since 27.08.2025
 * @author Nikita Kirillov
 */
public record EnvironmentPropertyResponse(String source, String value, List<PropertySource> propertySources) {

    public record PropertySource(String name, @Nullable Property property) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Property(String value, @Nullable String origin) {}
}
