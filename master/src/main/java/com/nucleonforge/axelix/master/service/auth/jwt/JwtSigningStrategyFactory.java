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
package com.nucleonforge.axelix.master.service.auth.jwt;

import java.util.Objects;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;

import com.nucleonforge.axelix.common.auth.core.JwtAlgorithm;

/**
 * Factory for creating {@link JwtSigningStrategy} instances
 * based on the specified {@link JwtAlgorithm}.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public class JwtSigningStrategyFactory {

    private JwtSigningStrategyFactory() {}

    @SuppressWarnings("NullAway")
    public static JwtSigningStrategy createSigningStrategy(JwtAlgorithm algorithm) {
        String algorithmName = Objects.requireNonNull(algorithm.getAlgorithmName(), "Algorithm name cannot be null");

        switch (algorithm) {
            case HMAC256, HMAC384, HMAC512 -> {
                MacAlgorithm macAlgorithm = (MacAlgorithm) Jwts.SIG.get().get(algorithmName);

                return new HmacSigningStrategy(macAlgorithm, algorithm.getMinKeyLength());
            }
            default ->
                throw new UnsupportedOperationException("Unsupported algorithm: " + algorithm.getAlgorithmName());
        }
    }
}
