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

import org.jspecify.annotations.Nullable;

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
     * @param inputs The binding input values and their origins.
     */
    public record Bean(String beanName, String prefix, List<Entry> properties, List<Entry> inputs) {}

    /**
     * @param key The flattened key in dot/bracket notation.
     * @param value The value, if available.
     */
    public record Entry(String key, @Nullable String value) {}
}
