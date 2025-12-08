/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.service.auth.jwt;

import io.jsonwebtoken.JwtBuilder;

import com.nucleonforge.axile.master.exception.auth.JwtTokenGenerationException;

/**
 * Strategy interface for signing JWT tokens with a specific signing key.
 *
 * <p>Implementations of this interface are responsible for applying digital signatures
 * to JWT tokens using cryptographic algorithms and key types.</p>
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public interface JwtSigningStrategy {

    /**
     * Applies a digital signature to the JWT token using the provided signing key.
     *
     * @param builder the JWT builder to apply the signature to
     * @param signingKey the key used for signing the token
     * @return the signed JwtBuilder instance
     * @throws JwtTokenGenerationException if signing fails due to invalid key,
     *         weak key, or other cryptographic issues
     */
    JwtBuilder signToken(JwtBuilder builder, String signingKey) throws JwtTokenGenerationException;
}
