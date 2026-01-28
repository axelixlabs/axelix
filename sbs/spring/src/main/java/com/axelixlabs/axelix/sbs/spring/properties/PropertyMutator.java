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

import org.springframework.context.ApplicationContext;

/**
 * The interface that is capable to modify the {@link Property} value.
 * <p>
 * The implementations must reload all necessary {@link ApplicationContext} components that rely on this property.
 *
 * @since 07.04.25
 * @author Mikhail Polivakha
 */
public interface PropertyMutator {

    /**
     * Mutate the property
     *
     * @param propertyName the property name to be mutated
     * @param newValue the new value of the property
     */
    void mutate(String propertyName, String newValue);
}
