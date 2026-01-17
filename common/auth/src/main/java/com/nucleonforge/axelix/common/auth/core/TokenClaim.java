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
package com.nucleonforge.axelix.common.auth.core;

/**
 * Enum representing JWT token claim keys used for consistent naming
 * of custom claims within JWT payloads.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public enum TokenClaim {
    ROLES("roles"),
    ROLE_NAME("name"),
    AUTHORITIES("authorities");

    /**
     * The string value that will be used as the key when this claim is encoded in a token.
     * <p>
     * This represents the actual key name that will appear in the serialized token (e.g., JWT).
     * For example, if the encoding is "roles", the token will contain a claim like:
     * {@code "roles": ["ADMIN", "USER"]}
     * </p>
     * <p>
     * This value is typically used during token creation and parsing to ensure consistent
     * naming of claims across the system.
     * </p>
     */
    private final String encoding;

    TokenClaim(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }
}
