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
package com.axelixlabs.axelix.master.autoconfiguration.auth;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.client.RestClient;

import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.service.AuthorityResolver;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.WebIdentityAccessManager;
import com.axelixlabs.axelix.common.domain.function.ThrowingCallable;
import com.axelixlabs.axelix.common.domain.function.ThrowingRunnable;
import com.axelixlabs.axelix.master.api.external.response.settings.AuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.LocalAuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.OidcAuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.SuperAdminAuthenticationOption;
import com.axelixlabs.axelix.master.filter.auth.CookieBasedJwtAuthorizationFilter;
import com.axelixlabs.axelix.master.mcp.auth.handler.BasicMcpAuthenticationHandler;
import com.axelixlabs.axelix.master.mcp.auth.handler.BearerMcpAuthenticationHandler;
import com.axelixlabs.axelix.master.mcp.auth.handler.McpAuthenticationHandler;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.MasterAuthorityResolver;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcMetadataProvider;
import com.axelixlabs.axelix.master.service.auth.provider.DatabaseUserAuthenticator;
import com.axelixlabs.axelix.master.service.auth.provider.SuperAdminUserAuthenticator;
import com.axelixlabs.axelix.master.service.state.UserService;

import static com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration.SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SecurityAutoConfiguration}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
class SecurityAutoConfigurationTest {

    private static final String SUPER_ADMIN_USERNAME = "test-super-admin";
    private static final String SUPER_ADMIN_PASSWORD = "test-super-admin-secret";

    private static String[] jwtPropertyValues() {
        return new String[] {
            "axelix.master.auth.jwt.algorithm=HMAC512",
            "axelix.master.auth.jwt.signing-key=secret",
            "axelix.master.auth.jwt.lifespan=PT30M",
        };
    }

    private static String[] superAdminCredentialPropertyValues() {
        return new String[] {
            SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX + ".credentials.username=" + SUPER_ADMIN_USERNAME,
            SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX + ".credentials.password=" + SUPER_ADMIN_PASSWORD,
        };
    }

    /**
     * Baseline context: JWT, cookie, core security beans, and super-admin login (always on when
     * {@link SecurityAutoConfiguration} is active). Optional {@code local} and {@code oauth2} login are off unless
     * enabled via properties and their auto-configurations are registered.
     */
    private static ApplicationContextRunner baselineConfigAppContextRunner() {
        return new ApplicationContextRunner(SecurityAutoConfigurationTest::isolatedContext)
                .withPropertyValues(jwtPropertyValues())
                .withPropertyValues(superAdminCredentialPropertyValues())
                .withUserConfiguration(TestSecurityDependenciesConfig.class)
                .withConfiguration(AutoConfigurations.of(
                        ConfigurationPropertiesAutoConfiguration.class,
                        SecurityAutoConfiguration.class,
                        SecurityAutoConfiguration.JwtAutoConfiguration.class,
                        SecurityAutoConfiguration.CookieAutoConfiguration.class));
    }

    @Nested
    class EnclosingClassConfigurationTests {

        @Test
        void shouldCreateAllBeansInDefaultScenario() {
            // given.
            ApplicationContextRunner contextRunner = baselineConfigAppContextRunner();

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasSingleBean(MasterAuthorityResolver.class);
                assertThat(context).hasSingleBean(AuthorityResolver.class);
                assertThat(context).hasSingleBean(Authorizer.class);
                assertThat(context).hasSingleBean(WebIdentityAccessManager.class);
                assertThat(context).hasSingleBean(JwtEncoderService.class);
                assertThat(context).hasSingleBean(JwtDecoderService.class);
                assertThat(context).hasSingleBean(CookieService.class);
                assertThat(context).hasSingleBean(CookieBasedJwtAuthorizationFilter.class);
                assertThat(context).hasSingleBean(SuperAdminUserAuthenticator.class);
                assertThat(context)
                        .getBeans(AuthenticationOption.class)
                        .hasSize(1)
                        .containsValue(context.getBean(SuperAdminAuthenticationOption.class));
            });
        }

        @Test
        void shouldFailWhenJwtAlgorithmPropertyIsMissing() {
            // given.
            ApplicationContextRunner contextRunner = new ApplicationContextRunner(
                            SecurityAutoConfigurationTest::isolatedContext)
                    .withPropertyValues(
                            "axelix.master.auth.jwt.signing-key=secret", "axelix.master.auth.jwt.lifespan=PT30M")
                    .withPropertyValues(superAdminCredentialPropertyValues())
                    .withConfiguration(AutoConfigurations.of(
                            ConfigurationPropertiesAutoConfiguration.class,
                            SecurityAutoConfiguration.class,
                            SecurityAutoConfiguration.JwtAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure()).isInstanceOf(BeanCreationException.class);
            });
        }

        @Test
        void shouldFailWhenJwtSigningKeyPropertyIsMissing() {
            // given.
            ApplicationContextRunner contextRunner = new ApplicationContextRunner(
                            SecurityAutoConfigurationTest::isolatedContext)
                    .withPropertyValues(
                            "axelix.master.auth.jwt.algorithm=HMAC512", "axelix.master.auth.jwt.lifespan=PT30M")
                    .withPropertyValues(superAdminCredentialPropertyValues())
                    .withConfiguration(AutoConfigurations.of(
                            ConfigurationPropertiesAutoConfiguration.class,
                            SecurityAutoConfiguration.class,
                            SecurityAutoConfiguration.JwtAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure()).isInstanceOf(BeanCreationException.class);
            });
        }

        @Test
        void shouldPassSuccessfullyWhenLifespanPropertyIsMissing() {
            // given.
            ApplicationContextRunner contextRunner = new ApplicationContextRunner(
                            SecurityAutoConfigurationTest::isolatedContext)
                    .withPropertyValues(
                            "axelix.master.auth.jwt.algorithm=HMAC512", "axelix.master.auth.jwt.signing-key=secret")
                    .withPropertyValues(superAdminCredentialPropertyValues())
                    .withUserConfiguration(TestSecurityDependenciesConfig.class)
                    .withConfiguration(AutoConfigurations.of(
                            ConfigurationPropertiesAutoConfiguration.class,
                            SecurityAutoConfiguration.class,
                            SecurityAutoConfiguration.JwtAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasNotFailed();
                assertThat(context).hasSingleBean(JwtEncoderService.class);
                assertThat(context).hasSingleBean(JwtDecoderService.class);
                assertThat(context).hasSingleBean(WebIdentityAccessManager.class);
                assertThat(context).hasSingleBean(AuthorityResolver.class);
                assertThat(context).hasSingleBean(Authorizer.class);
            });
        }

        @Test
        void shouldFailWhenJwtAlgorithmIsNotSupported() {
            // given.
            ApplicationContextRunner contextRunner = new ApplicationContextRunner(
                            SecurityAutoConfigurationTest::isolatedContext)
                    .withPropertyValues(
                            "axelix.master.auth.jwt.algorithm=RSA512",
                            "axelix.master.auth.jwt.signing-key=secret",
                            "axelix.master.auth.jwt.lifespan=PT30M")
                    .withPropertyValues(superAdminCredentialPropertyValues())
                    .withConfiguration(AutoConfigurations.of(
                            ConfigurationPropertiesAutoConfiguration.class,
                            SecurityAutoConfiguration.class,
                            SecurityAutoConfiguration.JwtAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasFailed();
            });
        }

        @Test
        void shouldNotCreateBasicMcpAuthenticationHandlerWhenMcpServerDisabled() {
            // given.
            ApplicationContextRunner contextRunner = baselineConfigAppContextRunner();

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).doesNotHaveBean(BasicMcpAuthenticationHandler.class);
                assertThat(context).doesNotHaveBean(McpAuthenticationHandler.class);
            });
        }

        @Test
        void shouldCreateBasicMcpAuthenticationHandlerWhenMcpServerEnabled() {
            // given.
            ApplicationContextRunner contextRunner =
                baselineConfigAppContextRunner().withPropertyValues("axelix.master.mcp-server.enabled=true");

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasSingleBean(BasicMcpAuthenticationHandler.class);
                assertThat(context).hasSingleBean(McpAuthenticationHandler.class);
            });
        }
    }

    @Nested
    class SuperAdminLoginConfigurationTests {

        @Test
        void shouldFailWhenSuperAdminCredentialsAreMissing() {
            // given.
            ApplicationContextRunner contextRunner = new ApplicationContextRunner(
                            SecurityAutoConfigurationTest::isolatedContext)
                    .withPropertyValues(jwtPropertyValues())
                    .withUserConfiguration(TestSecurityDependenciesConfig.class)
                    .withConfiguration(AutoConfigurations.of(
                            ConfigurationPropertiesAutoConfiguration.class,
                            SecurityAutoConfiguration.class,
                            SecurityAutoConfiguration.JwtAutoConfiguration.class,
                            SecurityAutoConfiguration.CookieAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasFailed();
            });
        }
    }

    @Nested
    class LocalLoginConfigurationTests {

        @Test
        void shouldCreateLocalLoginBeansWhenEnabled() {
            // given.
            ApplicationContextRunner contextRunner = baselineConfigAppContextRunner()
                    .withPropertyValues("axelix.master.auth.options.local.enabled=true")
                    .withConfiguration(
                            AutoConfigurations.of(SecurityAutoConfiguration.LocalLoginAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasSingleBean(DatabaseUserAuthenticator.class);
                assertThat(context).hasSingleBean(LocalAuthenticationOption.class);
                assertThat(context).hasSingleBean(SuperAdminUserAuthenticator.class);
                assertThat(context.getBeansOfType(AuthenticationOption.class).values())
                        .hasSize(2)
                        .anySatisfy(option -> assertThat(option).isInstanceOf(SuperAdminAuthenticationOption.class))
                        .anySatisfy(option -> assertThat(option).isInstanceOf(LocalAuthenticationOption.class));
            });
        }

        @Test
        void shouldNotCreateLocalLoginBeansWhenLocalDisabled() {
            // given.
            ApplicationContextRunner contextRunner = baselineConfigAppContextRunner()
                    .withPropertyValues("axelix.master.auth.options.local.enabled=false")
                    .withConfiguration(
                            AutoConfigurations.of(SecurityAutoConfiguration.LocalLoginAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(DatabaseUserAuthenticator.class);
                assertThat(context).doesNotHaveBean(LocalAuthenticationOption.class);
                assertThat(context).hasSingleBean(SuperAdminUserAuthenticator.class);
                assertThat(context)
                        .getBeans(AuthenticationOption.class)
                        .hasSize(1)
                        .containsValue(context.getBean(SuperAdminAuthenticationOption.class));
            });
        }
    }

    @Nested
    class OAuth2ConfigurationTests {

        @Test
        void shouldCreateBearerMcpAuthenticationHandlerWhenMcpServerEnabled() {
            // given.
            ApplicationContextRunner contextRunner = baselineConfigAppContextRunner()
                    .withPropertyValues(
                            "axelix.master.mcp-server.enabled=true",
                            "axelix.master.auth.options.oauth2.enabled=true",
                            "axelix.master.auth.options.oauth2.issuer-uri=https://issuer.example",
                            "axelix.master.auth.options.oauth2.client-id=test-client",
                            "axelix.master.auth.options.oauth2.client-secret=test-secret",
                            "axelix.master.auth.options.oauth2.base-url=https://app.example",
                            "axelix.master.auth.options.oauth2.scopes=openid profile")
                    .withUserConfiguration(TestOAuth2DependenciesConfig.class)
                    .withConfiguration(
                            AutoConfigurations.of(SecurityAutoConfiguration.OAuth2LoginAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasSingleBean(BasicMcpAuthenticationHandler.class);
                assertThat(context).hasSingleBean(BearerMcpAuthenticationHandler.class);
                assertThat(context).getBeans(McpAuthenticationHandler.class).hasSize(2);
            });
        }

        @Test
        void shouldCreateOAuth2BeansWhenEnabled() {
            // given.
            ApplicationContextRunner contextRunner = baselineConfigAppContextRunner()
                    .withPropertyValues(
                            "axelix.master.auth.options.oauth2.enabled=true",
                            "axelix.master.auth.options.oauth2.issuer-uri=https://issuer.example",
                            "axelix.master.auth.options.oauth2.client-id=test-client",
                            "axelix.master.auth.options.oauth2.client-secret=test-secret",
                            "axelix.master.auth.options.oauth2.base-url=https://app.example",
                            "axelix.master.auth.options.oauth2.scopes=openid profile")
                    .withUserConfiguration(TestOAuth2DependenciesConfig.class)
                    .withConfiguration(
                            AutoConfigurations.of(SecurityAutoConfiguration.OAuth2LoginAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasSingleBean(OidcMetadataProvider.class);
                assertThat(context).hasSingleBean(OidcClient.class);
                assertThat(context).doesNotHaveBean(BearerMcpAuthenticationHandler.class);
                assertThat(context.getBeansOfType(AuthenticationOption.class).values())
                        .hasSize(2)
                        .anySatisfy(option -> assertThat(option).isInstanceOf(SuperAdminAuthenticationOption.class))
                        .anySatisfy(option -> assertThat(option).isInstanceOf(OidcAuthenticationOption.class));
            });
        }

        @Test
        void shouldNotCreateOAuth2BeansWhenDisabled() {
            // given.
            ApplicationContextRunner contextRunner = baselineConfigAppContextRunner()
                    .withPropertyValues("axelix.master.auth.options.oauth2.enabled=false")
                    .withUserConfiguration(TestOAuth2DependenciesConfig.class)
                    .withConfiguration(
                            AutoConfigurations.of(SecurityAutoConfiguration.OAuth2LoginAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(OidcMetadataProvider.class);
                assertThat(context).doesNotHaveBean(OidcClient.class);
                assertThat(context)
                        .getBeans(AuthenticationOption.class)
                        .hasSize(1)
                        .containsValue(context.getBean(SuperAdminAuthenticationOption.class));
            });
        }

        @Test
        void shouldFailWhenOAuth2RequiredPropertiesAreMissing() {
            // given.
            ApplicationContextRunner contextRunner = baselineConfigAppContextRunner()
                    .withPropertyValues(
                            "axelix.master.auth.options.oauth2.enabled=true",
                            "axelix.master.auth.options.oauth2.client-id=test-client",
                            "axelix.master.auth.options.oauth2.client-secret=test-secret",
                            "axelix.master.auth.options.oauth2.base-url=https://app.example/callback")
                    .withUserConfiguration(TestOAuth2DependenciesConfig.class)
                    .withConfiguration(
                            AutoConfigurations.of(SecurityAutoConfiguration.OAuth2LoginAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasFailed();
            });
        }
    }

    @Nested
    class AuthenticationOptionCompositionTests {

        @Test
        void shouldCreateSuperAdminLocalAndOidcOptionsWhenLocalAndOauth2Enabled() {
            // given.
            ApplicationContextRunner contextRunner = baselineConfigAppContextRunner()
                    .withPropertyValues(
                            "axelix.master.auth.options.local.enabled=true",
                            "axelix.master.auth.options.oauth2.enabled=true",
                            "axelix.master.auth.options.oauth2.issuer-uri=https://issuer.example",
                            "axelix.master.auth.options.oauth2.client-id=test-client",
                            "axelix.master.auth.options.oauth2.client-secret=test-secret",
                            "axelix.master.auth.options.oauth2.base-url=https://app.example/callback",
                            "axelix.master.auth.options.oauth2.scopes=openid profile")
                    .withUserConfiguration(TestOAuth2DependenciesConfig.class)
                    .withConfiguration(AutoConfigurations.of(
                            SecurityAutoConfiguration.LocalLoginAutoConfiguration.class,
                            SecurityAutoConfiguration.OAuth2LoginAutoConfiguration.class));

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).getBeans(AuthenticationOption.class).hasSize(3);
                assertThat(context.getBeansOfType(AuthenticationOption.class).values())
                        .anySatisfy(option -> assertThat(option).isInstanceOf(SuperAdminAuthenticationOption.class))
                        .anySatisfy(option -> assertThat(option).isInstanceOf(LocalAuthenticationOption.class))
                        .anySatisfy(option -> assertThat(option).isInstanceOf(OidcAuthenticationOption.class));
            });
        }
    }

    @TestConfiguration
    static class TestSecurityDependenciesConfig {

        @Bean
        public SecurityContextExecutor securityContextExecutor() {
            return new NoOpSecurityContextExecutor();
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @TestConfiguration
    static class TestOAuth2DependenciesConfig {

        @Bean
        public RestClient restClient() {
            return RestClient.builder().build();
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    static final class NoOpSecurityContextExecutor implements SecurityContextExecutor {

        @Override
        public <T extends Exception> void runWithinSecurityContext(
                ThrowingRunnable<T> runnable, SecurityContext securityContext) throws T {
            runnable.run();
        }

        @Override
        public <V, T extends Exception> V callWithinSecurityContext(
                ThrowingCallable<V, T> callable, SecurityContext securityContext) throws T {
            return callable.call();
        }

        @Override
        public Optional<SecurityContext> getSecurityContext() {
            return Optional.empty();
        }
    }

    private static @NonNull AnnotationConfigApplicationContext isolatedContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        StandardEnvironment cleanEnv = new StandardEnvironment();
        cleanEnv.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        cleanEnv.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        context.setEnvironment(cleanEnv);
        return context;
    }
}
