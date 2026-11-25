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
package com.axelixlabs.axelix.master.api.external.response.configprops;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.api.external.response.PropertyFindingResponse;

/**
 * The configuration properties of a service.
 *
 * @author Nikita Kirillov
 */
public record ConfigurationPropertiesFeedResponse(List<Bean> beans) {

    /**
     * @param beanName The bean name.
     * @param prefix The prefix applied to the bean's property names.
     * @param properties The bound properties of the bean.
     */
    public record Bean(String beanName, String prefix, List<Entry> properties) {}

    /**
     * @param key The flattened key in dot/bracket notation.
     * @param value The value, if available.
     * @param findings The findings that matched this property.
     */
    public record Entry(String key, @Nullable String value, List<PropertyFindingResponse> findings) {

        // Compatibility field for console versions that consume a single dangerous value.
        @JsonProperty("dangerousValue")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public @Nullable PropertyFindingResponse dangerousValue() {
            return findings.isEmpty() ? null : findings.getFirst();
        }
    }
}
