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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.common.auth.JwtAlgorithm;
import com.nucleonforge.axile.common.auth.exception.ExpiredJwtTokenException;
import com.nucleonforge.axile.common.auth.exception.InvalidJwtTokenException;
import com.nucleonforge.axile.common.auth.rbac.jwt.service.RbacJwtDecoderService;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link RbacJwtDecoderService}, verifying correct decoding and validation of JWT tokens.
 *
 * @author Nikita Kirillov
 * @since 19.12.2025
 */
@SpringBootTest(classes = DefaultBasicJwtDecoderServiceTest.DefaultBasicJwtDecoderServiceTestConfiguration.class)
class DefaultBasicJwtDecoderServiceTest {

    @Autowired
    private BasicJwtDecoderService jwtDecoderService;

    @Value("${basic-auth-test-tokens.valid-token}")
    private String validToken;

    @Value("${basic-auth-test-tokens.token-with-hs256-algorithm}")
    private String validTokenWithHs256Algorithm;

    @Value("${basic-auth-test-tokens.token-with-hs384-algorithm}")
    private String validTokenWithHs384Algorithm;

    @Value("${basic-auth-test-tokens.expired-token}")
    private String expiredToken;

    @Value("${basic-auth-test-tokens.token-signed-with-wrong-key}")
    private String tokenSignedWithWrongKey;

    @Test
    void shouldDecodeValidJwtToken() {
        assertThatCode(() -> {
                    jwtDecoderService.isValidToken(validToken);
                })
                .doesNotThrowAnyException();
    }

    @Test
    void shouldEncodeDecodeTokenWithHS256() {
        String key256 = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";
        BasicJwtDecoderService decoder256 = new DefaultBasicJwtDecoderService(JwtAlgorithm.HMAC256, key256);

        assertThatCode(() -> {
                    decoder256.isValidToken(validTokenWithHs256Algorithm);
                })
                .doesNotThrowAnyException();
    }

    @Test
    void shouldEncodeDecodeTokenWithHS384() {
        String key384 =
                "bfa30eb1f16c07ba0a6a19a60f7c4bc02e1e10670411ae7a2f206b2bfe8801e2bb40741469d95fbbf4c86ae4b4a68437";
        BasicJwtDecoderService decoder384 = new DefaultBasicJwtDecoderService(JwtAlgorithm.HMAC384, key384);

        assertThatCode(() -> {
                    decoder384.isValidToken(validTokenWithHs384Algorithm);
                })
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowOnExpiredToken() {
        assertThatThrownBy(() -> jwtDecoderService.isValidToken(expiredToken))
                .isInstanceOf(ExpiredJwtTokenException.class);
    }

    @Test
    void shouldThrowOnTamperedToken() {
        String tamperedToken = validToken + "x";

        assertThatThrownBy(() -> jwtDecoderService.isValidToken(tamperedToken))
                .isInstanceOf(InvalidJwtTokenException.class);
    }

    @Test
    void shouldFailToDecodeTokenWithWrongSecret() {
        assertThatThrownBy(() -> jwtDecoderService.isValidToken(tokenSignedWithWrongKey))
                .isInstanceOf(InvalidJwtTokenException.class);
    }

    @TestConfiguration
    public static class DefaultBasicJwtDecoderServiceTestConfiguration {

        @Bean
        public BasicJwtDecoderService basicJwtDecoderService(
                final @Value("${axile.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axile.master.auth.jwt.signing-key}") String signingKey) {
            return new DefaultBasicJwtDecoderService(algorithm, signingKey);
        }
    }
}
