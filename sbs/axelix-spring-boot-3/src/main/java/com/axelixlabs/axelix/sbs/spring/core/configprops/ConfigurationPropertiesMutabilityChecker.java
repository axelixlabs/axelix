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

import org.springframework.boot.context.properties.ConfigurationPropertiesBean;

/**
 * Checks whether a configuration properties bean can be mutated at runtime.
 *
 * @author Nikita Kirillov
 */
public class ConfigurationPropertiesMutabilityChecker {

    /**
     * Determines whether the given configuration properties bean should be considered
     * immutable and excluded from runtime modifications.
     */
    public boolean isNotMutable(ConfigurationPropertiesBean bean) {
        String name = bean.getInstance().getClass().getName();
        return name.startsWith("org.springframework") && !name.startsWith("org.springframework.samples")
                || name.startsWith("com.zaxxer");
    }
}
