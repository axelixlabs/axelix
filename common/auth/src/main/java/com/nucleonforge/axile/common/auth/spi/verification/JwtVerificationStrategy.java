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
package com.nucleonforge.axile.common.auth.spi.verification;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

/**
 * Strategy interface for verifying and parsing JWT tokens with a specific signing key.
 *
 * <p>Implementations of this interface are responsible for validating JWT tokens,
 * verifying their digital signatures, and returning the parsed claims if verification succeeds.</p>
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public interface JwtVerificationStrategy {

    /**
     * Verifies and parses a JWT token using the provided signing key.
     *
     * @param token the JWT token to verify and parse
     * @param signingKey the key used to verify the token's signature
     * @return verified JWT claims in JWS format
     * @throws JwtException if verification fails (invalid signature, expired token, etc.)
     * @throws IllegalArgumentException if the token or key is malformed
     */
    Jws<Claims> verifyAndParse(String token, String signingKey) throws JwtException, IllegalArgumentException;
}
