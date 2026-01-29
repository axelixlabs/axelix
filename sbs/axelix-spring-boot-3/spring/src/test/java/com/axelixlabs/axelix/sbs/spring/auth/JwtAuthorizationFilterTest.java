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
package com.axelixlabs.axelix.sbs.spring.auth;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.common.auth.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.sbs.spring.beans.AxelixBeansEndpoint;
import com.axelixlabs.axelix.sbs.spring.beans.BeanMetaInfoExtractor;
import com.axelixlabs.axelix.sbs.spring.beans.BeansFeedBuilder;
import com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractor;
import com.axelixlabs.axelix.sbs.spring.beans.QualifiersPersistencePostProcessor;
import com.axelixlabs.axelix.sbs.spring.conditions.ConditionalBeanRefBuilder;
import com.axelixlabs.axelix.sbs.spring.conditions.DefaultConditionalBeanRefBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link JwtAuthorizationFilter}.
 * <p>
 * The tests here assume that some actuator management endpoints are exposed, for instance via:
 * <pre>
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include:
 *           - axelix-beans
 * </pre>
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @since 28.07.2025
 */
@SpringBootTest(
        classes = JwtAuthorizationFilterTest.JwtAuthorizationFilterTestConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtAuthorizationFilterTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${test-tokens.token-user-with-two-role}")
    private String tokenUserWithTwoRole;

    @Value("${test-tokens.token-user-with-admin-role-hierarchy}")
    private String tokenUserWithAdminRoleHierarchy;

    @Value("${test-tokens.token-with-empty-roles}")
    private String tokenWithEmptyRoles;

    @Value("${test-tokens.token-with-empty-authorities}")
    private String tokenWithoutAuthorities;

    @Value("${test-tokens.expired-token}")
    private String expiredToken;

    @Value("${test-tokens.token-with-invalid-authority}")
    private String tokenWithInvalidAuthority;

    @Value("${test-tokens.token-signed-with-wrong-key}")
    private String tokenSignedWithWrongKey;

    @Value("${test-tokens.token-with-null-name-roles}")
    private String tokenWithNullNameRoles;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldAllowAccess_UserHasSingleRoleWithRequiredAuthorities() {
        HttpEntity<Void> entity = defaultEntity(tokenUserWithTwoRole);

        ResponseEntity<String> responseBeans =
                restTemplate.exchange("/actuator/axelix-beans", HttpMethod.GET, entity, String.class);

        assertThat(responseBeans.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> responseMetrics =
                restTemplate.exchange("/actuator/health", HttpMethod.GET, entity, String.class);

        assertThat(responseMetrics.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldAllowAccess_UserHasMultipleRolesWithRequiredAuthorities() {
        HttpEntity<Void> entity = defaultEntity(tokenUserWithAdminRoleHierarchy);

        ResponseEntity<String> responseEnv =
                restTemplate.exchange("/actuator/env", HttpMethod.GET, entity, String.class);

        assertThat(responseEnv.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> responseMappings =
                restTemplate.exchange("/actuator/axelix-beans", HttpMethod.GET, entity, String.class);

        assertThat(responseMappings.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnForbidden_UserWithEmptyRoles() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/axelix-beans", HttpMethod.GET, defaultEntity(tokenWithEmptyRoles), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull().contains("Access denied: missing required authorities ");
    }

    @Test
    void shouldReturnForbidden_UserWithoutRequiredAuthority() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/axelix-env", HttpMethod.GET, defaultEntity(tokenWithoutAuthorities), String.class);

        assertThat(response)
                .returns(HttpStatus.FORBIDDEN, ResponseEntity::getStatusCode)
                .returns("Access denied: missing required authorities [ENV]", ResponseEntity::getBody);
    }

    @Test
    void shouldReturnForbidden_UserHasRoleWithInvalidAuthority() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/axelix-beans", HttpMethod.GET, defaultEntity(tokenWithInvalidAuthority), String.class);

        assertThat(response)
                .returns(HttpStatus.FORBIDDEN, ResponseEntity::getStatusCode)
                .returns("Access denied: missing required authorities [BEANS]", ResponseEntity::getBody);
    }

    @Test
    void shouldReturnUnauthorized_AuthorizationHeaderIsMalformed() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "BearerToken" + tokenUserWithTwoRole);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/axelix-beans", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorized_TokenIsTampered() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/axelix-beans", HttpMethod.GET, defaultEntity(tokenWithInvalidAuthority + "x"), String.class);

        assertThat(response)
                .returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode)
                .returns("JWT token is invalid or tampered", ResponseEntity::getBody);
    }

    @Test
    void shouldReturnUnauthorized_TokenSigningKeyIsTampered() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/axelix-beans", HttpMethod.GET, defaultEntity(tokenSignedWithWrongKey), String.class);

        assertThat(response)
                .returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode)
                .returns("JWT token is invalid or tampered", ResponseEntity::getBody);
    }

    @Test
    void shouldReturnUnauthorized_TokenIsExpired() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/axelix-beans", HttpMethod.GET, defaultEntity(expiredToken), String.class);

        assertThat(response)
                .returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode)
                .returns("JWT token has expired", ResponseEntity::getBody);
    }

    @Test
    void shouldReturnUnauthorized_TokenIsMissing() {
        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/health", HttpMethod.GET, defaultEntity(""), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorized_AuthorizationHeaderIsMissing() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/health", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnForbidden_TokenWithNullNameRoles() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/axelix-beans", HttpMethod.GET, defaultEntity(tokenWithNullNameRoles), String.class);

        assertThat(response).returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode);
    }

    private HttpEntity<Void> defaultEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return new HttpEntity<>(headers);
    }

    /**
     * Minimal test configuration for {@link JwtAuthorizationFilter} integration testing.
     *
     * <p>Registers required beans including {@link JwtDecoderService}, and
     * {@link JwtAuthorizationFilter} for use in the test suite.
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class JwtAuthorizationFilterTestConfiguration {

        @Bean
        public ConditionalBeanRefBuilder beanNameNormalizer() {
            return new DefaultConditionalBeanRefBuilder();
        }

        @Bean
        @ConditionalOnMissingBean
        public static QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
            return new QualifiersPersistencePostProcessor();
        }

        @Bean
        public BeanMetaInfoExtractor defaultBeanMetaInfoExtractor(
                ConfigurableApplicationContext configurableApplicationContext,
                ConditionalBeanRefBuilder conditionalBeanRefBuilder) {
            return new DefaultBeanMetaInfoExtractor(configurableApplicationContext, conditionalBeanRefBuilder);
        }

        @Bean
        public JwtDecoderService jwtDecoderService(
                final @Value("${axelix.sbs.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axelix.sbs.auth.jwt.signing-key}") String signingKey) {
            return new DefaultJwtDecoderService(algorithm, signingKey);
        }

        @Bean
        public AuthorityResolver authorityResolver() {
            return new DefaultAuthorityResolver();
        }

        @Bean
        public Authorizer authorizer() {
            return new DefaultAuthorizer();
        }

        @Bean
        public JwtAuthorizationFilter jwtAuthorizationFilter(
                JwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
            return new JwtAuthorizationFilter(jwtDecoderService, authorityResolver, authorizer);
        }

        @Bean
        public BeansFeedBuilder noOpBeanFeedBuilder() {
            return () -> new BeansFeed(Map.of());
        }

        @Bean
        public AxelixBeansEndpoint beansEndpointExtension(BeansFeedBuilder noOpBeanFeedBuilder) {
            return new AxelixBeansEndpoint(noOpBeanFeedBuilder);
        }

        @Bean
        public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
                JwtAuthorizationFilter filter) {
            FilterRegistrationBean<JwtAuthorizationFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(filter);
            registration.setName("jwtAuthorizationFilter");
            registration.addUrlPatterns("/actuator/*");
            return registration;
        }
    }
}
