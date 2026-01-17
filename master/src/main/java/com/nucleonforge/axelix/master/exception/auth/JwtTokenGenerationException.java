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
package com.nucleonforge.axelix.master.exception.auth;

import com.nucleonforge.axelix.master.service.auth.jwt.JwtEncoderService;

/**
 * The exception that happened during the JWT token generation process.
 *
 * @see JwtEncoderService
 * @since 23.07.2025
 * @author Nikita Kirillov
 */
public class JwtTokenGenerationException extends RuntimeException {

    public JwtTokenGenerationException(final String message) {
        super(message);
    }

    public JwtTokenGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
