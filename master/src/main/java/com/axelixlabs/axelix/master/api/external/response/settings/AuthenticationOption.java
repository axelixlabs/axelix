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
package com.axelixlabs.axelix.master.api.external.response.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Interface that represent a specific authentication option.
 *
 * @since 06.03.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public sealed interface AuthenticationOption
        permits OidcAuthenticationOption, SuperAdminAuthenticationOption, LocalAuthenticationOption {

    /**
     * Returns the name of the authentication option.
     *
     * @return the type identifier, e.g. {@code "oidc"} or {@code login-password}
     */
    String type();

    /**
     * Whether this authentication option can currently be offered to the user.
     * <p>
     * Options that depend on an external system (e.g. an OIDC provider) may become temporarily
     * unavailable. Such options are excluded from the settings response so that the UI still loads
     * and the remaining options stay usable, instead of failing the whole response.
     *
     * @return {@code true} if the option should be exposed, {@code false} to omit it
     */
    @JsonIgnore
    default boolean isAvailable() {
        return true;
    }
}
