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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Enum representing supported JWT signing algorithms,
 * along with their required minimum key lengths.
 *
 * @since 25.07.2025
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public enum JwtAlgorithm {
    HMAC256(32, "HS256"),
    HMAC384(48, "HS384"),
    HMAC512(64, "HS512");

    /**
     * Minimum required key length in bytes for the algorithm.
     */
    private final int minKeyLength;

    /**
     * The standard JWT algorithm name.
     */
    private final String algorithmName;

    JwtAlgorithm(int minKeyLength, String algorithmName) {
        this.minKeyLength = minKeyLength;
        this.algorithmName = algorithmName;
    }

    public static JwtAlgorithm parse(String input) throws IllegalArgumentException {
        for (JwtAlgorithm value : values()) {
            if (value.algorithmName.equalsIgnoreCase(input) || value.name().equalsIgnoreCase(input)) {
                return value;
            }
        }

        throw new IllegalArgumentException(String.format(
                "Unrecognized value of the Jwt signature generation algorithm '%s'. Supported values are : %s",
                input, Arrays.toString(values())));
    }

    public int getMinKeyLength() {
        return minKeyLength;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * Verifies that {@code signingKey} meets the minimum length required by this algorithm.
     *
     * @param signingKey   the configured signing key
     * @param propertyName fully qualified configuration property name to include in error messages
     */
    public void validateSigningKey(String signingKey, String propertyName) {
        int keyBytes = signingKey.getBytes(StandardCharsets.UTF_8).length;

        if (keyBytes < minKeyLength) {
            throw new IllegalArgumentException("JWT signing-key is too short for " + algorithmName + " (requires at least "
                    + minKeyLength + " bytes). Set " + propertyName);
        }
    }
}
