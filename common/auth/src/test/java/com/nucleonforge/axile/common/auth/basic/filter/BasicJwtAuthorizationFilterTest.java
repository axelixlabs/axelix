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
package com.nucleonforge.axile.common.auth.basic.filter;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.common.auth.JwtAlgorithm;
import com.nucleonforge.axile.common.auth.Main;
import com.nucleonforge.axile.common.auth.basic.jwt.service.BasicJwtDecoderService;
import com.nucleonforge.axile.common.auth.basic.jwt.service.DefaultBasicJwtDecoderService;
import com.nucleonforge.axile.common.auth.rbac.filter.RbacJwtAuthorizationFilter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RbacJwtAuthorizationFilter}.
 *
 * @author Nikita Kirillov
 * @since 19.12.2025
 */
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "management.endpoints.web.exposure.include=beans")
@Import(BasicJwtAuthorizationFilterTest.BasicJwtAuthorizationFilterTestConfiguration.class)
class BasicJwtAuthorizationFilterTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${basic-auth-test-tokens.valid-token}")
    private String validToken;

    @Value("${basic-auth-test-tokens.expired-token}")
    private String expiredToken;

    @Value("${basic-auth-test-tokens.token-signed-with-wrong-key}")
    private String tokenSignedWithWrongKey;

    @Test
    void shouldAllowAccess_validToken() {
        HttpEntity<Void> entity = defaultEntity(validToken);

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/beans", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnUnauthorized_AuthorizationHeaderIsMalformed() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.SET_COOKIE, "BearerToken" + validToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/beans", HttpMethod.GET, entity, String.class);

        assertThat(response).returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode);
    }

    @Test
    void shouldReturnUnauthorized_TokenSigningKeyIsTampered() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/beans", HttpMethod.GET, defaultEntity(tokenSignedWithWrongKey), String.class);

        assertThat(response).returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode);
    }

    @Test
    void shouldReturnUnauthorized_TokenIsExpired() {
        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/beans", HttpMethod.GET, defaultEntity(expiredToken), String.class);

        assertThat(response).returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode);
    }

    @Test
    void shouldReturnUnauthorized_TokenIsMissing() {
        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/beans", HttpMethod.GET, defaultEntity(""), String.class);

        assertThat(response).returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode);
    }

    @Test
    void shouldReturnUnauthorized_AuthorizationHeaderIsMissing() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/beans", HttpMethod.GET, entity, String.class);

        assertThat(response).returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode);
    }

    private HttpEntity<Void> defaultEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.SET_COOKIE, token);

        return new HttpEntity<>(headers);
    }

    @TestConfiguration
    static class BasicJwtAuthorizationFilterTestConfiguration {

        @Bean
        public BasicJwtDecoderService basicJwtDecoderService(
                final @Value("${axile.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axile.master.auth.jwt.signing-key}") String signingKey) {
            return new DefaultBasicJwtDecoderService(algorithm, signingKey);
        }

        @Bean
        public BasicJwtAuthorizationFilter basicJwtAuthorizationFilter(
                BasicJwtDecoderService basicAuthJwtDecoderService) {
            return new BasicJwtAuthorizationFilter(basicAuthJwtDecoderService);
        }

        @Bean
        public FilterRegistrationBean<BasicJwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
                BasicJwtAuthorizationFilter filter) {
            FilterRegistrationBean<BasicJwtAuthorizationFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(filter);
            registration.setName("jwtAuthorizationFilter");
            registration.addUrlPatterns("/actuator/*");
            return registration;
        }
    }
}
