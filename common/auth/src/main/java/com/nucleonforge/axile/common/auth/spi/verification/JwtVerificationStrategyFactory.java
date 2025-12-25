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

import com.nucleonforge.axile.common.auth.JwtAlgorithm;

/**
 * Factory class for creating JWT verification strategy instances based on the specified algorithm.
 *
 * <p>This factory provides a centralized way to obtain appropriate verification strategies
 * for different JWT signing algorithms.</p>
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public class JwtVerificationStrategyFactory {

    private JwtVerificationStrategyFactory() {}

    public static JwtVerificationStrategy createVerificationStrategy(JwtAlgorithm algorithm) {
        return switch (algorithm) {
            case HMAC256, HMAC384, HMAC512 -> new HmacVerificationStrategy();
        };
    }
}
