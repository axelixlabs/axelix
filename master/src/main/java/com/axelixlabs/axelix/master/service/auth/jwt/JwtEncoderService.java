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
package com.axelixlabs.axelix.master.service.auth.jwt;

import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.JwtTokenGenerationException;

/**
 * Contract for generating signed JWT tokens from {@link User} representations.
 *
 * <p>Implementations of this interface serialize user identity and authorization
 * data into a secure JWT string.</p>
 *
 * @since 23.07.2025
 * @author Nikita Kirillov
 */
public interface JwtEncoderService {

    /**
     * Generates a signed JWT token from the given {@link User} object.
     *
     * <p>The resulting token encodes the user's identity and roles.</p>
     *
     * @param user the {@link User} to serialize into the token
     * @return a signed JWT token as a string
     * @throws JwtTokenGenerationException if the user is invalid or token creation fails
     */
    // TODO: add jspecify annotations for request/response
    String generateToken(User user) throws JwtTokenGenerationException;
}
