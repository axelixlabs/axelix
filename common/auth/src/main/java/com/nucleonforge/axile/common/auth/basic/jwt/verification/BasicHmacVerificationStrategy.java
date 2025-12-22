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
package com.nucleonforge.axile.common.auth.basic.jwt.verification;

import java.nio.charset.StandardCharsets;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * {@link BasicJwtVerificationStrategy} implementation that verifies and parses JWT tokens
 * using HMAC-SHA signing algorithms.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public class BasicHmacVerificationStrategy implements BasicJwtVerificationStrategy {

    @Override
    public void verifyAndParse(String token, String signingKey) throws JwtException, IllegalArgumentException {
        Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parse(token);
    }
}
