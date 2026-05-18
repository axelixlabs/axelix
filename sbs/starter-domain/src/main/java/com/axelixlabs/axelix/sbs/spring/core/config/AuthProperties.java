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
package com.axelixlabs.axelix.sbs.spring.core.config;

import java.time.Duration;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.utils.Assert;

/**
 * Configuration properties related to auth.
 *
 * @author Mikhail Polivakha
 */
@SuppressWarnings("NullAway.Init")
public class AuthProperties implements Validatable {

    /**
     * JWT related configuration
     */
    private Jwt jwt = new Jwt();

    @Override
    public void validate() throws IllegalArgumentException {
        Assert.notNull(jwt.getAlgorithm(), "The algorithm for the JWS cannot be null");
        Assert.notNull(jwt.getSigningKey(), "The singing key for the JWS cannot be null");
    }

    public static class Jwt {

        /**
         * The algorithm used for JWS creation inside the authentication tokens. Cannot be {@code null}.
         */
        private JwtAlgorithm algorithm;

        /**
         * The key that should be used when verifying the JWS. Cannot be {@code null}.
         */
        private String signingKey;

        /**
         * JWT token validity duration to be set when constructing the token.
         */
        private Duration duration = Duration.ofSeconds(300);

        public JwtAlgorithm getAlgorithm() {
            return algorithm;
        }

        public Jwt setAlgorithm(JwtAlgorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public String getSigningKey() {
            return signingKey;
        }

        public Jwt setSigningKey(String signingKey) {
            this.signingKey = signingKey;
            return this;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }
    }

    public Jwt getJwt() {
        return jwt;
    }

    public AuthProperties setJwt(Jwt jwt) {
        this.jwt = jwt;
        return this;
    }
}
