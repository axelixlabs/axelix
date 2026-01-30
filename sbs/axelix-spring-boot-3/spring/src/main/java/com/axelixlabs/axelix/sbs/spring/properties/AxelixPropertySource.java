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

import java.util.Map;

import org.springframework.core.env.MapPropertySource;

/**
 * A custom {@link MapPropertySource} implementation used to hold mutable property values
 * managed dynamically during application runtime.
 *
 * <p>This property source is registered under the name {@link AxelixPropertySource#AXELIX_PROPERTY_SOURCE_NAME}
 * and is used to override or introduce configuration properties dynamically, through
 * actuator endpoints.
 *
 * @since 07.04.2025
 * @author Mikhail Polivakha
 */
public class AxelixPropertySource extends MapPropertySource {

    public static final String AXELIX_PROPERTY_SOURCE_NAME = "AXELIX_PROPERTY_SOURCE_NAME";

    public AxelixPropertySource(Map<String, Object> source) {
        super(AXELIX_PROPERTY_SOURCE_NAME, source);
    }

    /**
     * Add new property value to property source
     *
     * @param name <strong>already resolved</strong> property name
     */
    public void addProperty(String name, String value) {
        super.source.put(name, value);
    }
}
