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
package com.nucleonforge.axelix.master.autoconfiguration.auth;

import java.time.Duration;

import com.nucleonforge.axelix.common.auth.core.JwtAlgorithm;

/**
 * JWT configuration properties.
 *
 * @since 11.12.2025
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@SuppressWarnings("NullAway.Init")
public class JwtProperties {

    private JwtAlgorithm algorithm;
    private String signingKey;
    private Duration lifespan;

    public JwtProperties setAlgorithm(JwtAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public JwtProperties setSigningKey(String signingKey) {
        this.signingKey = signingKey;
        return this;
    }

    public JwtProperties setLifespan(Duration lifespan) {
        this.lifespan = lifespan;
        return this;
    }

    public JwtAlgorithm getAlgorithm() {
        return algorithm;
    }

    public String getSigningKey() {
        return signingKey;
    }

    public Duration getLifespan() {
        return lifespan;
    }
}
