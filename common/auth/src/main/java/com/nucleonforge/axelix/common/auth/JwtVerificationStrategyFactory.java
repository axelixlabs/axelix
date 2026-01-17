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
package com.nucleonforge.axelix.common.auth;

import com.nucleonforge.axelix.common.auth.core.JwtAlgorithm;

/**
 * Factory class for creating JWT verification strategy instances based on the specified algorithm.
 *
 * <p>This factory provides a centralized way to obtain appropriate verification strategies
 * for different JWT signing algorithms.</p>
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public class JwtVerificationStrategyFactory {

    private JwtVerificationStrategyFactory() {}

    public static JwtVerificationStrategy createVerificationStrategy(JwtAlgorithm algorithm) {
        return switch (algorithm) {
            case HMAC256, HMAC384, HMAC512 -> new HmacVerificationStrategy();
        };
    }
}
