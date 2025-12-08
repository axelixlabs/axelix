/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.common.auth.spi.jwt;

import java.util.Arrays;

/**
 * Enum representing supported JWT signing algorithms,
 * along with their required minimum key lengths.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
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

        throw new IllegalArgumentException(
                "Unrecognized value of the Jwt signature generation algorithm '%s'. Supported values are : %s"
                        .formatted(input, Arrays.toString(values())));
    }

    public int getMinKeyLength() {
        return minKeyLength;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }
}
