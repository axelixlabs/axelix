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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.service.AuthorityResolver;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.auth.service.DefaultAuthorizer;
import com.axelixlabs.axelix.common.auth.service.DefaultIdentityAccessManager;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.IdentityAccessManager;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.sbs.spring.core.beans.AxelixBeansEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.beans.BeanMetaInfoExtractor;
import com.axelixlabs.axelix.sbs.spring.core.beans.BeansFeedBuilder;
import com.axelixlabs.axelix.sbs.spring.core.beans.DefaultBeanMetaInfoExtractor;
import com.axelixlabs.axelix.sbs.spring.core.beans.QualifiersPersistencePostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.cache.AxelixCachesEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.cache.CacheManagerBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.cache.CacheSizeProvider;
import com.axelixlabs.axelix.sbs.spring.core.cache.DefaultCacheOperationsDispatcher;
import com.axelixlabs.axelix.sbs.spring.core.cache.DefaultCacheSizeProvider;
import com.axelixlabs.axelix.sbs.spring.core.cache.EnhancedCacheManager;
import com.axelixlabs.axelix.sbs.spring.core.conditions.ConditionalBeanRefBuilder;
import com.axelixlabs.axelix.sbs.spring.core.conditions.DefaultConditionalBeanRefBuilder;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.env.AxelixEnvironmentEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentService;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentTestConfig;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;

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
 * @author Sergey Cherkasov
 * @since 28.07.2025
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axelix.prop.test.name=axelix-beans",
        })
@Import({
    JwtAuthorizationFilterTest.JwtAuthorizationFilterTestConfiguration.class,
    AxelixCachesEndpoint.class,
    DefaultCacheOperationsDispatcher.class,
    EnvironmentTestConfig.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JwtAuthorizationFilterTest {

    // Cache names under test
    private static final String TEST_CACHE_1 = "cache1";

    private static final String TEST_CACHE_2 = "cache2";

    private static final String TEST_CACHE_MANAGER = TEST_CACHE_2;

    private EnhancedCacheManager cacheManager;

    @Autowired
    // The bean definition in the context for cache manager has a type of CacheManager,
    // so we cannot do simple field injection via EnhancedCacheManager class.
    public JwtAuthorizationFilterTest setCacheManager(org.springframework.cache.CacheManager cacheManager) {
        this.cacheManager = (EnhancedCacheManager) cacheManager;
        return this;
    }

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    void setUp() {
        cacheManager.enableAll();

        for (String cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).invalidate();
        }
    }

    private static final String USER_NAME = "testUser";
    private static final String PASSWORD = "testPassword";

    @Value("${axelix.sbs.auth.jwt.lifespan}")
    private Duration lifespan;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtEncoderService jwtEncoderService;

    @ParameterizedTest
    @MethodSource("adminEndpoints")
    void shouldAllowAccess_ForUserWithRoleAdmin(String path, HttpMethod httpMethod) {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN));
        HttpEntity<Void> entity = defaultEntity(jwtEncoderService.generateToken(user));
        String key1 = "key1";
        Cache cache = cacheManager.getCache(TEST_CACHE_1);
        cache.put(key1, "value1");

        ResponseEntity<Void> response = testRestTemplate.exchange(path, httpMethod, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("adminEndpoints")
    void shouldAllowAccess_MultipleRoles(String path, HttpMethod httpMethod) {
        User user =
                new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN, DefaultRole.EDITOR, DefaultRole.VIEWER));
        HttpEntity<Void> entity = defaultEntity(jwtEncoderService.generateToken(user));
        String key1 = "key1";
        Cache cache = cacheManager.getCache(TEST_CACHE_1);
        cache.put(key1, "value1");

        ResponseEntity<Void> response = testRestTemplate.exchange(path, httpMethod, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("editorEndpoints")
    void shouldAllowAccess_ForUserWithRoleEditor(String path, HttpMethod httpMethod) {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.EDITOR));
        HttpEntity<Void> entity = defaultEntity(jwtEncoderService.generateToken(user));
        String key1 = "key1";
        Cache cache = cacheManager.getCache(TEST_CACHE_1);
        cache.put(key1, "value1");

        ResponseEntity<Void> response = testRestTemplate.exchange(path, httpMethod, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("viewerEndpoints")
    void shouldAllowAccess_ForUserWithRoleViewer(String path, HttpMethod httpMethod) {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.VIEWER));
        HttpEntity<Void> entity = defaultEntity(jwtEncoderService.generateToken(user));

        ResponseEntity<Void> response = testRestTemplate.exchange(path, httpMethod, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("viewerEndpoints")
    void shouldAllowAccess_UserWithEmptyRoles(String path, HttpMethod httpMethod) {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of());
        HttpEntity<Void> entity = defaultEntity(jwtEncoderService.generateToken(user));

        String key1 = "key1";
        Cache cache = cacheManager.getCache(TEST_CACHE_1);
        cache.put(key1, "value1");

        ResponseEntity<String> response = restTemplate.exchange(path, httpMethod, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnForbidden_UserWithoutRequiredAuthority_ForUserWithRoleEditor() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.EDITOR));
        HttpEntity<Void> entity = defaultEntity(jwtEncoderService.generateToken(user));

        ResponseEntity<Void> response =
                testRestTemplate.exchange("/actuator/axelix-env", HttpMethod.GET, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @ParameterizedTest
    @MethodSource("notAuthorityForRole")
    void shouldReturnForbidden_UserWithoutRequiredAuthority_ForUserWithRoleViewer(String path, HttpMethod httpMethod) {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.VIEWER));
        HttpEntity<Void> entity = defaultEntity(jwtEncoderService.generateToken(user));

        String key1 = "key1";
        Cache cache = cacheManager.getCache(TEST_CACHE_1);
        cache.put(key1, "value1");

        ResponseEntity<Void> response = testRestTemplate.exchange(path, httpMethod, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @ParameterizedTest
    @MethodSource("notAuthorityForRole")
    void shouldReturnForbidden_UserWithEmptyRoles(String path, HttpMethod httpMethod) {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of());
        HttpEntity<Void> entity = defaultEntity(jwtEncoderService.generateToken(user));

        String key1 = "key1";
        Cache cache = cacheManager.getCache(TEST_CACHE_1);
        cache.put(key1, "value1");

        ResponseEntity<String> response = restTemplate.exchange(path, httpMethod, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturnUnauthorized_AuthorizationHeaderIsMalformed() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.VIEWER));
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "BearerToken" + jwtEncoderService.generateToken(user));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/axelix-beans", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnForbidden_UserHasRoleWithInvalidAuthority() {
        Role role = new DefaultRole("VIEWER", Set.of(UnrecognizedAuthority.UNRECOGNIZED_AUTHORITY));
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(role));
        String token = jwtEncoderService.generateToken(user);

        String key1 = "key1";
        Cache cache = cacheManager.getCache(TEST_CACHE_1);
        cache.put(key1, "value1");

        ResponseEntity<Void> response = testRestTemplate.exchange(
                path(TEST_CACHE_1 + "/clear?key=key1"), HttpMethod.DELETE, defaultEntity(token), Void.class);

        assertThat(response).returns(HttpStatus.FORBIDDEN, ResponseEntity::getStatusCode);
    }

    @Test
    void shouldReturnUnauthorized_TokenIsTampered() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN));
        String token = jwtEncoderService.generateToken(user);

        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/axelix-beans", HttpMethod.GET, defaultEntity(token + "x"), String.class);

        assertThat(response).returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode);
    }

    @Test
    void shouldReturnUnauthorized_TokenSigningKeyIsTampered() {
        String wrongSecret = "MX3TNBx0j8bGCjGWCvq1JffIqqzXLIV-URlKFLX4mfA";
        JwtEncoderService encoderWithWrongSecret =
                new DefaultJwtEncoderService(JwtAlgorithm.HMAC256, wrongSecret, lifespan);

        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of());
        String token = encoderWithWrongSecret.generateToken(user);

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/axelix-beans", HttpMethod.GET, defaultEntity(token), String.class);

        assertThat(response).returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode);
    }

    @Test
    void shouldReturnUnauthorized_TokenIsExpired() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN));
        String token = jwtEncoderService.generateToken(user, Duration.ofDays(0));

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/axelix-beans", HttpMethod.GET, defaultEntity(token), String.class);

        assertThat(response).returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode);
    }

    @Test
    void shouldReturnUnauthorized_TokenIsMissing() {
        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/axelix-beans", HttpMethod.GET, defaultEntity(""), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnAllowNonAxelixActuatorEndpointToBeInvokedWithoutToken() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnForbidden_TokenWithNullNameRoles() {
        Role role = new DefaultRole(null, Set.of(DefaultAuthority.CACHES_CLEAR));
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(role));
        String token = jwtEncoderService.generateToken(user);

        ResponseEntity<String> response =
                restTemplate.exchange("/actuator/axelix-beans", HttpMethod.GET, defaultEntity(token), String.class);

        assertThat(response).returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode);
    }

    static Stream<Arguments> adminEndpoints() {
        return Stream.of(
            Arguments.of("/actuator/axelix-caches", HttpMethod.GET),
            Arguments.of(path(TEST_CACHE_1), HttpMethod.GET),
            Arguments.of(path(TEST_CACHE_1 + "/clear?key=key1"), HttpMethod.DELETE),
            Arguments.of(path(TEST_CACHE_1 + "/clear"), HttpMethod.DELETE),
            Arguments.of(path("/clear-all"), HttpMethod.DELETE),
            Arguments.of(path("/disable"), HttpMethod.POST),
            Arguments.of(path("/enable"), HttpMethod.POST),
            Arguments.of(path(TEST_CACHE_1 + "/disable"), HttpMethod.POST),
            Arguments.of(path(TEST_CACHE_1 + "/enable"), HttpMethod.POST),
            Arguments.of("/actuator/axelix-env", HttpMethod.GET),
            Arguments.of("/actuator/axelix-beans", HttpMethod.GET));
    }

    static Stream<Arguments> editorEndpoints() {
        return Stream.of(
            Arguments.of("/actuator/axelix-caches", HttpMethod.GET),
            Arguments.of(path(TEST_CACHE_1), HttpMethod.GET),
            Arguments.of(path(TEST_CACHE_1 + "/clear?key=key1"), HttpMethod.DELETE),
            Arguments.of(path(TEST_CACHE_1 + "/clear"), HttpMethod.DELETE),
            Arguments.of(path("/clear-all"), HttpMethod.DELETE),
            Arguments.of(path("/disable"), HttpMethod.POST),
            Arguments.of(path("/enable"), HttpMethod.POST),
            Arguments.of(path(TEST_CACHE_1 + "/disable"), HttpMethod.POST),
            Arguments.of(path(TEST_CACHE_1 + "/enable"), HttpMethod.POST),
            Arguments.of("/actuator/axelix-beans", HttpMethod.GET));
    }

    static Stream<Arguments> viewerEndpoints() {
        return Stream.of(
            Arguments.of("/actuator/axelix-caches", HttpMethod.GET),
            Arguments.of(path(TEST_CACHE_1), HttpMethod.GET),
            Arguments.of("/actuator/axelix-beans", HttpMethod.GET));
    }

    static Stream<Arguments> notAuthorityForRole() {
        return Stream.of(
            Arguments.of(path(TEST_CACHE_1 + "/clear?key=key1"), HttpMethod.DELETE),
            Arguments.of(path(TEST_CACHE_1 + "/clear"), HttpMethod.DELETE),
            Arguments.of(path("/clear-all"), HttpMethod.DELETE),
            Arguments.of(path("/disable"), HttpMethod.POST),
            Arguments.of(path("/enable"), HttpMethod.POST),
            Arguments.of(path(TEST_CACHE_1 + "/disable"), HttpMethod.POST),
            Arguments.of(path(TEST_CACHE_1 + "/enable"), HttpMethod.POST),
            Arguments.of("/actuator/axelix-env", HttpMethod.GET));
    }

    private HttpEntity<Void> defaultEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, AuthenticationSchemes.BEARER.code() + " " + token);

        return new HttpEntity<>(headers);
    }

    enum UnrecognizedAuthority implements Authority {
        UNRECOGNIZED_AUTHORITY;

        @Override
        public String getName() {
            return name();
        }
    }

    private static String path(String relative) {
        return path(TEST_CACHE_MANAGER, relative);
    }

    private static String path(String cacheManagerName, String relative) {
        relative = prefixPathIfNeeded(relative);
        cacheManagerName = prefixPathIfNeeded(cacheManagerName);

        return "/actuator/axelix-caches" + cacheManagerName + relative;
    }

    private static String prefixPathIfNeeded(String path) {
        return (path.isEmpty() || path.charAt(0) == '/') ? path : "/" + path;
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
        public ConditionalBeanRefBuilder conditionalBeanRefBuilder() {
            return new DefaultConditionalBeanRefBuilder();
        }

        @Bean
        public static QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
            return new QualifiersPersistencePostProcessor();
        }

        @Bean
        public BeanMetaInfoExtractor beanMetaInfoExtractor(
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
        JwtEncoderService jwtEncoderService(
                final @Value("${axelix.sbs.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axelix.sbs.auth.jwt.lifespan}") Duration lifespan,
                final @Value("${axelix.sbs.auth.jwt.signing-key}") String signingKey) {
            return new DefaultJwtEncoderService(algorithm, signingKey, lifespan);
        }

        @Bean
        public AuthorityResolver authorityResolver() {
            return new DefaultAuthorityResolver((pathTemplate, actualPath) -> {
                PathPattern parse = new PathPatternParser().parse("/actuator" + pathTemplate);
                return parse.matchAndExtract(PathContainer.parsePath(actualPath)) != null;
            });
        }

        @Bean
        public Authorizer authorizer() {
            return new DefaultAuthorizer();
        }

        @Bean
        public BeansFeedBuilder noOpBeanFeedBuilder() {
            return () -> new BeansFeed(List.of());
        }

        @Bean
        public AxelixBeansEndpoint axelixBeansEndpoint(BeansFeedBuilder noOpBeanFeedBuilder) {
            return new AxelixBeansEndpoint(noOpBeanFeedBuilder);
        }

        @Bean
        public IdentityAccessManager securityManager(
                JwtDecoderService jwtDecoderService,
                AuthorityResolver authorityResolver,
                Authorizer authorizer,
                ConfigurableEnvironment environment) {
            return new DefaultIdentityAccessManager(jwtDecoderService, authorityResolver, authorizer);
        }

        @Bean
        public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
                IdentityAccessManager identityAccessManager, SecurityContextExecutor securityContextExecutor) {
            var registration = new FilterRegistrationBean<>(
                    new JwtAuthorizationFilter(identityAccessManager, securityContextExecutor));
            registration.setName("jwtAuthorizationFilter");
            return registration;
        }

        @Bean
        public EndpointsConfigurationProperties endpointsConfigurationProperties() {
            return new EndpointsConfigurationProperties();
        }

        @Bean
        public AxelixEnvironmentEndpoint axelixEnvironmentEndpoint(EnvironmentService environmentService) {
            return new AxelixEnvironmentEndpoint(environmentService);
        }

        @Bean
        public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(
                    List.of("axelix.env.test.toBeSanitized", "AXELIX_FOR_SANITIZATION"), propertyNameNormalizer);
        }

        @Bean
        @ConditionalOnMissingBean
        public CacheSizeProvider cacheSizeProvider() {
            return new DefaultCacheSizeProvider();
        }

        @Bean
        public static CacheManagerBeanPostProcessor cacheManagerBeanPostProcessor() {
            return new CacheManagerBeanPostProcessor();
        }

        @Bean(name = TEST_CACHE_MANAGER)
        public org.springframework.cache.CacheManager testSubjectCacheManager() {
            return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
        }
    }
}
