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
package com.axelixlabs.axelix.sbs.spring.env;

import org.jspecify.annotations.Nullable;

/**
 * Metadata for a Spring Boot property, including description and deprecation info.
 *
 * @param description the property description.
 * @param deprecation deprecation related information. If {@code null}, the
 *                    property is not considered deprecated. If not {@code null},
 *                    then the property is considered deprecated.
 *
 * @since 04.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public record PropertyMetadata(@Nullable String description, @Nullable Deprecation deprecation) {

    /**
     * Deprecation metadata for a property.
     *
     * @param message explaining why the property is deprecated and, optionally, what should be used instead.
     */
    public record Deprecation(String message) {}
}
