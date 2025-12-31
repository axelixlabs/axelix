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
