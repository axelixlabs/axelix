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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.sbs.spring.core.auth.AuthorityResolver;
import com.axelixlabs.axelix.sbs.spring.core.auth.Authorizer;
import com.axelixlabs.axelix.sbs.spring.core.auth.DefaultAuthorityResolver;
import com.axelixlabs.axelix.sbs.spring.core.auth.DefaultAuthorizer;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthorizationFilter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link JwtAuthAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
 */
class JwtAuthAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    "axelix.sbs.auth.jwt",
                    "axelix.sbs.auth.jwt.algorithm=HMAC512",
                    "axelix.sbs.auth.jwt.signing-key=secret")
            .withConfiguration(AutoConfigurations.of(JwtAuthAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(JwtAuthAutoConfiguration.class);
            assertThat(context).hasSingleBean(JwtDecoderService.class);
            assertThat(context).hasSingleBean(AuthorityResolver.class);
            assertThat(context).hasSingleBean(Authorizer.class);
            assertThat(context).hasSingleBean(JwtAuthorizationFilter.class);
            assertThat(context).hasSingleBean(FilterRegistrationBean.class);
        });
    }

    @Test
    void shouldFail_whenAlgorithmPropertyIsMissing() {
        new ApplicationContextRunner()
                // "axelix.sbs.auth.jwt.algorithm" is missing
                .withPropertyValues("axelix.sbs.auth.jwt", "axelix.sbs.auth.jwt.signing-key=secret")
                .withConfiguration(AutoConfigurations.of(JwtAuthAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).isInstanceOf(BeanCreationException.class);
                });
    }

    @Test
    void shouldFail_whenSigningKeyPropertyIsMissing() {
        new ApplicationContextRunner()
                // "axelix.sbs.auth.jwt.signing-key" is missing
                .withPropertyValues("axelix.sbs.auth.jwt", "axelix.sbs.auth.jwt.algorithm=HMAC512")
                .withConfiguration(AutoConfigurations.of(JwtAuthAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).isInstanceOf(BeanCreationException.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfiguration_whenJwtPropertyIsMissing() {
        new ApplicationContextRunner()
                // "axelix.sbs.auth.jwt" is missing
                .withPropertyValues("axelix.sbs.auth.jwt.algorithm=HMAC512", "axelix.sbs.auth.jwt.signing-key=secret")
                .withConfiguration(AutoConfigurations.of(JwtAuthAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(JwtAuthAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(JwtDecoderService.class);
                    assertThat(context).doesNotHaveBean(AuthorityResolver.class);
                    assertThat(context).doesNotHaveBean(Authorizer.class);
                    assertThat(context).doesNotHaveBean(JwtAuthorizationFilter.class);
                });
    }

    @Test
    void shouldFail_whenAlgorithmIsNotSupported() {
        new ApplicationContextRunner()
                // "axelix.sbs.auth.jwt.algorithm=RSA512" algorithm not supported
                .withPropertyValues(
                        "axelix.sbs.auth.jwt",
                        "axelix.sbs.auth.jwt.algorithm=RSA512",
                        "axelix.sbs.auth.jwt.signing-key=secret")
                .withConfiguration(AutoConfigurations.of(JwtAuthAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).isInstanceOf(BeanCreationException.class);
                });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomJwtDecoderServiceConfig.class,
                        CustomAuthorityResolverConfig.class,
                        CustomAuthorizerConfig.class)
                .run(context -> {
                    assertThat(context.getBean(JwtDecoderService.class))
                            .isExactlyInstanceOf(CustomJwtDecoderService.class);
                    assertThat(context.getBean(AuthorityResolver.class))
                            .isExactlyInstanceOf(CustomAuthorityResolver.class);
                    assertThat(context.getBean(Authorizer.class)).isExactlyInstanceOf(CustomAuthorizer.class);
                });
    }

    @TestConfiguration
    static class CustomJwtDecoderServiceConfig {
        @Bean
        public JwtDecoderService jwtDecoderService() {
            return new CustomJwtDecoderService();
        }
    }

    @TestConfiguration
    static class CustomAuthorityResolverConfig {
        @Bean
        public AuthorityResolver authorityResolver() {
            return new CustomAuthorityResolver();
        }
    }

    @TestConfiguration
    static class CustomAuthorizerConfig {
        @Bean
        public Authorizer authorizer() {
            return new CustomAuthorizer();
        }
    }

    @TestConfiguration
    static class CustomJwtAuthorizationFilterConfig {
        @Bean
        public JwtAuthorizationFilter jwtAuthorizationFilter(
                JwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
            return new CustomJwtAuthorizationFilter(jwtDecoderService, authorityResolver, authorizer);
        }
    }

    static class CustomJwtDecoderService extends DefaultJwtDecoderService {
        public CustomJwtDecoderService() {
            super(JwtAlgorithm.HMAC512, "secret");
        }
    }

    static class CustomAuthorityResolver extends DefaultAuthorityResolver {}

    static class CustomAuthorizer extends DefaultAuthorizer {}

    static class CustomJwtAuthorizationFilter extends JwtAuthorizationFilter {
        public CustomJwtAuthorizationFilter(
                JwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
            super(jwtDecoderService, authorityResolver, authorizer);
        }
    }
}
