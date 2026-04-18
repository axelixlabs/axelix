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
package com.axelixlabs.axelix.master.autoconfiguration;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.service.AuthorityResolver;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.auth.service.IdentityAccessManager;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.domain.function.ThrowingCallable;
import com.axelixlabs.axelix.common.domain.function.ThrowingRunnable;
import com.axelixlabs.axelix.master.api.external.response.settings.AuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.LoginPasswordAuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.OidcAuthenticationOption;
import com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration;
import com.axelixlabs.axelix.master.filter.CookieBasedJwtAuthorizationFilter;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.MasterAuthorityResolver;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcMetadataProvider;
import com.axelixlabs.axelix.master.service.auth.provider.StaticAdminUserProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SecurityAutoConfiguration}.
 *
 * @author Sergey Cherkasov
 */
class SecurityAutoConfigurationTest {

    private static ApplicationContextRunner jwtAndCookieContextRunner() {
        return new ApplicationContextRunner()
                .withPropertyValues(
                        "axelix.master.auth.jwt.algorithm=HMAC512",
                        "axelix.master.auth.jwt.signing-key=secret",
                        "axelix.master.auth.jwt.lifespan=PT30M")
                .withUserConfiguration(TestSecurityDependenciesConfig.class)
                .withConfiguration(AutoConfigurations.of(
                        ConfigurationPropertiesAutoConfiguration.class,
                        SecurityAutoConfiguration.class,
                        SecurityAutoConfiguration.JwtAutoConfiguration.class,
                        SecurityAutoConfiguration.CookieAutoConfiguration.class));
    }

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        ApplicationContextRunner contextRunner = jwtAndCookieContextRunner();

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasSingleBean(MasterAuthorityResolver.class);
            assertThat(context).hasSingleBean(AuthorityResolver.class);
            assertThat(context).hasSingleBean(Authorizer.class);
            assertThat(context).hasSingleBean(IdentityAccessManager.class);
            assertThat(context).hasSingleBean(JwtEncoderService.class);
            assertThat(context).hasSingleBean(JwtDecoderService.class);
            assertThat(context).hasSingleBean(CookieService.class);
            assertThat(context).hasSingleBean(CookieBasedJwtAuthorizationFilter.class);
        });
    }

    @Test
    void shouldFailWhenJwtAlgorithmPropertyIsMissing() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withPropertyValues(
                        "axelix.master.auth.jwt.signing-key=secret", "axelix.master.auth.jwt.lifespan=PT30M")
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
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withPropertyValues("axelix.master.auth.jwt.algorithm=HMAC512", "axelix.master.auth.jwt.lifespan=PT30M")
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
    void shouldPassSuccessfullyWhenAllPropertiesPresent() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withPropertyValues(
                        "axelix.master.auth.jwt.algorithm=HMAC512",
                        "axelix.master.auth.jwt.signing-key=secret",
                        "axelix.master.auth.jwt.lifespan=PT30M")
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
            assertThat(context).hasSingleBean(IdentityAccessManager.class);
            assertThat(context).hasSingleBean(AuthorityResolver.class);
            assertThat(context).hasSingleBean(Authorizer.class);
        });
    }

    @Test
    void shouldPassSuccessfullyWhenLifespanPropertyIsMissing() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withPropertyValues(
                        "axelix.master.auth.jwt.algorithm=HMAC512", "axelix.master.auth.jwt.signing-key=secret")
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
            assertThat(context).hasSingleBean(IdentityAccessManager.class);
            assertThat(context).hasSingleBean(AuthorityResolver.class);
            assertThat(context).hasSingleBean(Authorizer.class);
        });
    }

    @Test
    void shouldFailWhenJwtAlgorithmIsNotSupported() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withPropertyValues(
                        "axelix.master.auth.jwt.algorithm=RSA512",
                        "axelix.master.auth.jwt.signing-key=secret",
                        "axelix.master.auth.jwt.lifespan=PT30M")
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
    void shouldUseCustomJwtDecoderServiceWhenProvided() {
        ApplicationContextRunner contextRunner =
                jwtAndCookieContextRunner().withUserConfiguration(CustomJwtDecoderServiceConfig.class);

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(JwtDecoderService.class)).isExactlyInstanceOf(CustomJwtDecoderService.class);
        });
    }

    @Test
    void shouldCreateStaticAdminBeansWhenEnabled() {
        ApplicationContextRunner contextRunner = jwtAndCookieContextRunner()
                .withPropertyValues(
                        "axelix.master.auth.options.static-admin.enabled=true",
                        "axelix.master.auth.options.static-admin.credentials.username=admin",
                        "axelix.master.auth.options.static-admin.credentials.password=secret")
                .withConfiguration(AutoConfigurations.of(staticCredentialsConfigClass()));

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasSingleBean(StaticAdminUserProvider.class);
            assertThat(context).hasBean("authSettingsStaticAdmin");
            assertThat(context.getBean("authSettingsStaticAdmin"))
                    .isInstanceOf(LoginPasswordAuthenticationOption.class);
        });
    }

    @Test
    void shouldNotCreateStaticAdminBeansWhenDisabled() {
        ApplicationContextRunner contextRunner = jwtAndCookieContextRunner()
                .withPropertyValues("axelix.master.auth.options.static-admin.enabled=false")
                .withConfiguration(AutoConfigurations.of(staticCredentialsConfigClass()));

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(StaticAdminUserProvider.class);
            assertThat(context).doesNotHaveBean("authSettingsStaticAdmin");
        });
    }

    @Test
    void shouldFailWhenStaticAdminCredentialsAreMissing() {
        ApplicationContextRunner contextRunner = jwtAndCookieContextRunner()
                .withPropertyValues("axelix.master.auth.options.static-admin.enabled=true")
                .withConfiguration(AutoConfigurations.of(staticCredentialsConfigClass()));

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasFailed();
        });
    }

    @Test
    void shouldCreateOAuth2BeansWhenEnabled() {
        ApplicationContextRunner contextRunner = jwtAndCookieContextRunner()
                .withPropertyValues(
                        "axelix.master.auth.options.oauth2.enabled=true",
                        "axelix.master.auth.options.oauth2.issuer-uri=https://issuer.example",
                        "axelix.master.auth.options.oauth2.client-id=test-client",
                        "axelix.master.auth.options.oauth2.client-secret=test-secret",
                        "axelix.master.auth.options.oauth2.redirect-uri=https://app.example/callback",
                        "axelix.master.auth.options.oauth2.scopes=openid profile")
                .withUserConfiguration(TestOAuth2DependenciesConfig.class)
                .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.OAuth2Config.class));

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasSingleBean(OidcMetadataProvider.class);
            assertThat(context).hasSingleBean(OidcClient.class);
            assertThat(context).hasBean("authSettingsOAuth2");
            assertThat(context.getBean("authSettingsOAuth2")).isInstanceOf(OidcAuthenticationOption.class);
        });
    }

    @Test
    void shouldNotCreateOAuth2BeansWhenDisabled() {
        ApplicationContextRunner contextRunner = jwtAndCookieContextRunner()
                .withPropertyValues("axelix.master.auth.options.oauth2.enabled=false")
                .withUserConfiguration(TestOAuth2DependenciesConfig.class)
                .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.OAuth2Config.class));

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(OidcMetadataProvider.class);
            assertThat(context).doesNotHaveBean(OidcClient.class);
            assertThat(context).doesNotHaveBean("authSettingsOAuth2");
        });
    }

    @Test
    void shouldFailWhenOAuth2RequiredPropertiesAreMissing() {
        ApplicationContextRunner contextRunner = jwtAndCookieContextRunner()
                .withPropertyValues(
                        "axelix.master.auth.options.oauth2.enabled=true",
                        "axelix.master.auth.options.oauth2.client-id=test-client",
                        "axelix.master.auth.options.oauth2.client-secret=test-secret",
                        "axelix.master.auth.options.oauth2.redirect-uri=https://app.example/callback")
                .withUserConfiguration(TestOAuth2DependenciesConfig.class)
                .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.OAuth2Config.class));

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasFailed();
        });
    }

    @Test
    void shouldCreateBothAuthenticationOptionsWhenBothModesEnabled() {
        ApplicationContextRunner contextRunner = jwtAndCookieContextRunner()
                .withPropertyValues(
                        "axelix.master.auth.options.static-admin.enabled=true",
                        "axelix.master.auth.options.static-admin.credentials.username=admin",
                        "axelix.master.auth.options.static-admin.credentials.password=secret",
                        "axelix.master.auth.options.oauth2.enabled=true",
                        "axelix.master.auth.options.oauth2.issuer-uri=https://issuer.example",
                        "axelix.master.auth.options.oauth2.client-id=test-client",
                        "axelix.master.auth.options.oauth2.client-secret=test-secret",
                        "axelix.master.auth.options.oauth2.redirect-uri=https://app.example/callback",
                        "axelix.master.auth.options.oauth2.scopes=openid profile")
                .withUserConfiguration(TestOAuth2DependenciesConfig.class)
                .withConfiguration(AutoConfigurations.of(
                        staticCredentialsConfigClass(), SecurityAutoConfiguration.OAuth2Config.class));

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).getBeans(AuthenticationOption.class).hasSize(2);
            assertThat(context.getBean("authSettingsStaticAdmin"))
                    .isInstanceOf(LoginPasswordAuthenticationOption.class);
            assertThat(context.getBean("authSettingsOAuth2")).isInstanceOf(OidcAuthenticationOption.class);
        });
    }

    @TestConfiguration
    static class TestSecurityDependenciesConfig {

        @Bean
        public SecurityContextExecutor securityContextExecutor() {
            return new NoOpSecurityContextExecutor();
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

    @TestConfiguration
    static class CustomJwtDecoderServiceConfig {

        @Bean
        public JwtDecoderService jwtDecoderService() {
            return new CustomJwtDecoderService();
        }
    }

    static class CustomJwtDecoderService implements JwtDecoderService {

        @Override
        public PasswordlessUser decodeTokenToUser(String token) {
            return new PasswordlessUser("custom-user", Set.of());
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

    private static Class<?> staticCredentialsConfigClass() {
        try {
            return Class.forName(
                    "com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration$StaticCredentialsConfig");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load StaticCredentialsConfig", e);
        }
    }
}
