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
package com.nucleonforge.axile.common.auth.basic.jwt.service;

import java.util.Objects;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import com.nucleonforge.axile.common.auth.JwtAlgorithm;
import com.nucleonforge.axile.common.auth.basic.jwt.verification.BasicJwtVerificationStrategy;
import com.nucleonforge.axile.common.auth.basic.jwt.verification.BasicJwtVerificationStrategyFactory;
import com.nucleonforge.axile.common.auth.exception.ExpiredJwtTokenException;
import com.nucleonforge.axile.common.auth.exception.InvalidJwtTokenException;
import com.nucleonforge.axile.common.auth.exception.JwtTokenDecodingException;

/**
 * Default implementation of {@link BasicJwtDecoderService}.
 *
 * @since 22.07.2025
 * @author Nikita Kirillov
 */
public class DefaultBasicJwtDecoderService implements BasicJwtDecoderService {

    private final BasicJwtVerificationStrategy verificationStrategy;

    private final String signingKey;

    public DefaultBasicJwtDecoderService(JwtAlgorithm algorithm, String signingKey) {
        this.verificationStrategy = BasicJwtVerificationStrategyFactory.createVerificationStrategy(algorithm);
        this.signingKey = Objects.requireNonNull(signingKey);
    }

    @Override
    public void isValidToken(String token)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtTokenDecodingException {

        try {
            verificationStrategy.verifyAndParse(token, signingKey);
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtTokenException("JWT token has expired", e);
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("JWT token is invalid or tampered", e);
        } catch (Exception e) {
            throw new JwtTokenDecodingException("Unexpected error while decoding JWT token", e);
        }
    }
}
