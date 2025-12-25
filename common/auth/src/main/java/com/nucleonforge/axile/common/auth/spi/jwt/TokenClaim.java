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

/**
 * Enum representing JWT token claim keys used for consistent naming
 * of custom claims within JWT payloads.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public enum TokenClaim {
    ROLES("roles"),
    ROLE_NAME("name"),
    AUTHORITIES("authorities");

    /**
     * The string value that will be used as the key when this claim is encoded in a token.
     * <p>
     * This represents the actual key name that will appear in the serialized token (e.g., JWT).
     * For example, if the encoding is "roles", the token will contain a claim like:
     * {@code "roles": ["ADMIN", "USER"]}
     * </p>
     * <p>
     * This value is typically used during token creation and parsing to ensure consistent
     * naming of claims across the system.
     * </p>
     */
    private final String encoding;

    TokenClaim(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }
}
