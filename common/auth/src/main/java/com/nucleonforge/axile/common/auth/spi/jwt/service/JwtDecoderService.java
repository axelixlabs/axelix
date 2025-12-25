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
package com.nucleonforge.axile.common.auth.spi.jwt.service;

import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.common.auth.exception.ExpiredJwtTokenException;
import com.nucleonforge.axile.common.auth.exception.InvalidJwtTokenException;
import com.nucleonforge.axile.common.auth.exception.JwtParsingException;
import com.nucleonforge.axile.common.auth.exception.JwtTokenDecodingException;
import com.nucleonforge.axile.common.auth.spi.jwt.model.DecodedUser;

/**
 * Contract for decoding and validating JWT tokens into {@link User} representations.
 *
 * @since 23.07.2025
 * @author Nikita Kirillov
 */
public interface JwtDecoderService {

    /**
     * Parses the given JWT token and converts it into a {@link User}.
     *
     * @param token the JWT token to decode
     * @return the reconstructed {@link User}
     * @throws ExpiredJwtTokenException if the JWT token has expired
     * @throws InvalidJwtTokenException if the JWT token is invalid or tampered with
     * @throws JwtTokenDecodingException if an unexpected error occurs during token decoding
     * @throws JwtParsingException if the token cannot be parsed or contains insufficient data
     */
    DecodedUser decodeTokenToUser(String token)
            throws ExpiredJwtTokenException, JwtTokenDecodingException, JwtParsingException, InvalidJwtTokenException;
}
