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
package com.nucleonforge.axile.master.service.auth.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import com.nucleonforge.axile.common.auth.JwtAlgorithm;
import com.nucleonforge.axile.common.auth.rbac.core.User;
import com.nucleonforge.axile.master.exception.auth.JwtTokenGenerationException;

/**
 * Service responsible for generating JWT tokens from {@link User} instances.
 *
 * @since 21.12.2025
 * @author Nikita Kirillov
 */
public class BasicJwtEncoderService implements JwtEncoderService {

    private final JwtSigningStrategy signingStrategy;

    private final String signingKey;

    private final Duration lifespan;

    public BasicJwtEncoderService(final JwtAlgorithm algorithm, final String signingKey, final Duration lifespan) {
        this.signingStrategy = JwtSigningStrategyFactory.createSigningStrategy(algorithm);
        this.signingKey = signingKey;
        this.lifespan = lifespan;
    }

    @Override
    public String generateToken(User user) throws JwtTokenGenerationException {
        validateUser(user);

        try {
            Instant now = Instant.now();

            JwtBuilder builder = Jwts.builder()
                    .subject(user.username())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(lifespan)));

            return signingStrategy.signToken(builder, signingKey).compact();
        } catch (JwtTokenGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtTokenGenerationException("Failed to generate JWT token", e);
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new JwtTokenGenerationException("User cannot be null");
        }

        if (user.username() == null || user.username().isEmpty()) {
            throw new JwtTokenGenerationException("Username cannot be null or empty");
        }
    }
}
