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
package com.axelixlabs.axelix.common.auth;

import com.axelixlabs.axelix.common.auth.core.DecodedUser;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;

/**
 * Contract for decoding and validating JWT tokens into {@link User} representations.
 *
 * @since 23.07.2025
 * @author Nikita Kirillov
 */
public interface JwtDecoderService {

    /**
     * Parses the given JWT token and converts it into a {@link User}.
     *
     * @param token the JWT token to decode
     * @return the reconstructed {@link User}
     * @throws ExpiredJwtTokenException if the JWT token has expired
     * @throws InvalidJwtTokenException if the JWT token is invalid or tampered with
     * @throws JwtParsingException if the token cannot be parsed or contains insufficient data
     */
    DecodedUser decodeTokenToUser(String token)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtParsingException;
}
