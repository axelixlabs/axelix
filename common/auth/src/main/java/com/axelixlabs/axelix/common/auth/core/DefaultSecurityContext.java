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
package com.axelixlabs.axelix.common.auth.core;

/**
 * Default implementation of the {@link SecurityContext}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultSecurityContext implements SecurityContext {

    private final User user;
    private final String token;

    public DefaultSecurityContext(User user, String token) {
        this.user = user;
        this.token = token;
    }

    @Override
    public User currentUser() {
        return user;
    }

    @Override
    public String token() {
        return token;
    }
}
