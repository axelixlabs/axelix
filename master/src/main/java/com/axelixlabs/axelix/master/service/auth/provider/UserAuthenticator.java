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
package com.axelixlabs.axelix.master.service.auth.provider;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.User;

/**
 * SPI interface that is capable to authenticate the {@link User} by his/her username and password credentials.
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public interface UserAuthenticator {

    /**
     * Authenticate user by username.
     *
     * @param username the user's username.
     * @param password the user's password.
     *
     * @return the authenticated {@link User}, or null, if the user with such credentials pair was not found.
     */
    @Nullable
    User authenticate(String username, String password);
}
