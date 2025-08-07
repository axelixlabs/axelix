package com.nucleonforge.axile.auth.filter;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
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

import com.nucleonforge.axile.auth.spi.Authorizer;
import com.nucleonforge.axile.auth.spi.DefaultAuthorizer;
import com.nucleonforge.axile.auth.spi.jwt.service.DefaultJwtDecoderService;
import com.nucleonforge.axile.auth.spi.jwt.service.JwtDecoderService;
import com.nucleonforge.axile.common.auth.spi.jwt.JwtAlgorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link JwtAuthorizationFilter}.
 *
 * <p>Note: these tests assume that management endpoints are fully exposed via the following configuration:</p>
 * <pre>
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: "*"
 * </pre>
 *
 * @author Nikita Kirillov
 * @since 28.07.2025
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JwtAuthorizationFilterTest.JwtAuthorizationFilterTestConfiguration.class)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
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

    @Test
    void shouldAllowAccess_UserHasSingleRoleWithRequiredAuthorities() {
        HttpEntity<Void> entity = defaultEntity(tokenUserWithTwoRole);

        ResponseEntity<String> responseBeans =
                restTemplate.exchange("/actuator/beans", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, responseBeans.getStatusCode());

        ResponseEntity<String> responseMetrics =
                restTemplate.exchange("/actuator/health", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, responseMetrics.getStatusCode());
    }

    @Test
    void shouldAllowAccess_UserHasMultipleRolesWithRequiredAuthorities() {
        HttpEntity<Void> entity = defaultEntity(tokenUserWithAdminRoleHierarchy);

        ResponseEntity<String> responseEnv =
                restTemplate.exchange("/actuator/env", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, responseEnv.getStatusCode());

        ResponseEntity<String> responseMappings =
                restTemplate.exchange("/actuator/info", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, responseMappings.getStatusCode());
    }

    @Test
    void shouldReturnForbidden_UserWithEmptyRoles() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/beans", HttpMethod.GET, defaultEntity(tokenWithEmptyRoles), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Access denied: missing required authorities "));
    }

    @Test
    void shouldReturnForbidden_UserWithoutRequiredAuthority() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/env", HttpMethod.GET, defaultEntity(tokenWithoutAuthorities), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Access denied: missing required authorities "));
    }

    @Test
    void shouldReturnForbidden_UserHasRoleButMissingRequiredAuthority() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/beans", HttpMethod.GET, defaultEntity(tokenWithInvalidAuthority), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Access denied: missing required authorities "));
    }

    @Test
    void shouldReturnUnauthorized_AuthorizationHeaderIsMalformed() {
        HttpHeaders headers = new HttpHeaders();

        headers.set(HttpHeaders.AUTHORIZATION, "BearerToken" + tokenUserWithTwoRole);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/beans", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Authorization token is missing", response.getBody());
    }

    @Test
    void shouldReturnUnauthorized_TokenIsTampered() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/beans", HttpMethod.GET, defaultEntity(tokenWithInvalidAuthority + "x"), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JWT token is invalid or tampered", response.getBody());
    }

    @Test
    void shouldReturnUnauthorized_TokenSigningKeyIsTampered() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/beans", HttpMethod.GET, defaultEntity(tokenSignedWithWrongKey), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("JWT token is invalid or tampered", response.getBody());
    }

    @Test
    void shouldReturnUnauthorized_TokenIsExpired() {
        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/beans", HttpMethod.GET, defaultEntity(expiredToken), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JWT token has expired", response.getBody());
    }

    @Test
    void shouldReturnForbidden_TokenIsMissing() {
        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/health", HttpMethod.GET, defaultEntity(""), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Authorization token is missing", response.getBody());
    }

    @Test
    void shouldReturnForbidden_AuthorizationHeaderIsMissing() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/health", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Authorization token is missing", response.getBody());
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
    @TestConfiguration
    static class JwtAuthorizationFilterTestConfiguration {

        @Bean
        public JwtDecoderService jwtDecoderService(
                final @Value("${axile.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axile.master.auth.jwt.signing-key}") String signingKey) {
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
