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
package com.axelixlabs.axelix.sbs.spring.core.env;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.Deprecation;

/**
 * Builds auxiliary mappings used during environment property processing.
 *
 * <p>This abstraction is responsible for deriving metadata-backed objects and
 * configuration-properties-to-bean mappings.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public interface PropertyMappingBuilder {

    /**
     * Builds {@link Deprecation} information from the provided property metadata.
     *
     * @param propertyMetadata metadata describing the property, may be {@code null}
     * @return deprecation descriptor derived from the metadata, or {@code null} if no deprecation is defined
     */
    @Nullable
    Deprecation buildFromMetadata(@Nullable PropertyMetadata propertyMetadata);

    /**
     * Builds a mapping between normalized configuration property names and bean names
     * that expose them via {@code @ConfigurationProperties}.
     *
     * @return map where the key is the normalized property name and the value is the owning bean name.
     */
    Map<String, String> buildConfigPropsMappingMap();
}
