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
package com.axelixlabs.axelix.common.auth.service.tech;

import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;

/**
 * Verifies and deserializes a JWT containing a {@link TechUser} subject.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public interface TechUserJwtDeserializer {

    /**
     * @param token signed JWT to deserialize
     * @return technical user identified by the token subject
     * @throws InvalidJwtTokenException if the token is invalid or tampered with
     * @throws JwtParsingException if the token cannot be parsed or its subject is not a technical user
     */
    TechUser deserialize(String token) throws InvalidJwtTokenException, JwtParsingException;
}
