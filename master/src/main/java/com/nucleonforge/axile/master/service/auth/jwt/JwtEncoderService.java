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

import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.master.exception.auth.JwtTokenGenerationException;

/**
 * Contract for generating signed JWT tokens from {@link User} representations.
 *
 * <p>Implementations of this interface serialize user identity and authorization
 * data into a secure JWT string.</p>
 *
 * @since 23.07.2025
 * @author Nikita Kirillov
 */
public interface JwtEncoderService {

    /**
     * Generates a signed JWT token from the given {@link User} object.
     *
     * <p>The resulting token encodes the user's identity and roles.</p>
     *
     * @param user the {@link User} to serialize into the token
     * @return a signed JWT token as a string
     * @throws JwtTokenGenerationException if the user is invalid or token creation fails
     */
    // TODO: add jspecify annotations for request/response
    String generateToken(User user) throws JwtTokenGenerationException;
}
