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

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

/**
 * Interface for discovering the actual property name as it is stored in
 * the {@link ConfigurableEnvironment}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public interface PropertyNameDiscoverer {

    /**
     * Discovers the primary property name.
     * <p>
     * If the property name exists in multiple {@link PropertySource Property Sources}, the resulting name will be
     * from the property source with the highest priority.
     *
     * @param propertyName property name in any form (upper/lower case, dots/underscores/dashes separated etc.)
     * @return discovered {@code propertyName}, or {@code null} if no property with the given name is not found
     */
    @Nullable
    String discover(String propertyName);
}
