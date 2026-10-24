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
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration;

import com.axelixlabs.axelix.common.auth.core.JwtDefaults;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.sbs.spring.core.auth.AuthorityResolver;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthorizationFilter;
import com.axelixlabs.axelix.sbs.spring.core.auth.ReactiveJwtAuthorizationFilter;
import com.axelixlabs.axelix.sbs.spring.core.auth.WebIdentityAccessManager;
import com.axelixlabs.axelix.sbs.spring.core.config.AuthProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link JwtAuthAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
class JwtAuthAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withPropertyValues(
                    "axelix.sbs.auth.jwt",
                    "axelix.sbs.auth.jwt.algorithm=HMAC512",
                    "axelix.sbs.auth.jwt.signing-key=secret")
            .withConfiguration(AutoConfigurations.of(
                    JwtAuthAutoConfiguration.class, SecurityContextExecutorAutoConfiguration.class));

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JwtAuthAutoConfiguration.class,
                    SecurityContextExecutorAutoConfiguration.class,
                    EndpointAutoConfiguration.class,
                    WebEndpointAutoConfiguration.class,
                    DispatcherServletAutoConfiguration.class,
                    ManagementContextAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(JwtAuthAutoConfiguration.class);
            assertThat(context).hasSingleBean(JwtDecoderService.class);
            assertThat(context).hasSingleBean(JwtEncoderService.class);
            assertThat(context).hasSingleBean(AuthorityResolver.class);
            assertThat(context).hasSingleBean(Authorizer.class);
            assertThat(context).hasSingleBean(WebIdentityAccessManager.class);
            assertThat(context).hasSingleBean(SecurityContextExecutor.class);
        });
    }

    @Test
    void shouldRegisterReactiveFilterInMainContext_whenApplicationIsReactive() {
        new ReactiveWebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JwtAuthAutoConfiguration.class,
                        SecurityContextExecutorAutoConfiguration.class,
                        EndpointAutoConfiguration.class,
                        WebEndpointAutoConfiguration.class,
                        ManagementContextAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(ReactiveJwtAuthorizationFilter.class);
                    assertThat(context).doesNotHaveBean(FilterRegistrationBean.class);
                });
    }

    @Test
    void shouldUseDefaults_whenAlgorithmAndSigningKeyAreMissing() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JwtAuthAutoConfiguration.class, SecurityContextExecutorAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();

                    AuthProperties.Jwt jwt =
                            context.getBean(AuthProperties.class).getJwt();
                    assertThat(jwt.getAlgorithm()).isEqualTo(JwtDefaults.DEFAULT_ALGORITHM);
                    assertThat(jwt.getSigningKey()).isEqualTo(JwtDefaults.DEFAULT_SIGNING_KEY);
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
                .withConfiguration(AutoConfigurations.of(
                        JwtAuthAutoConfiguration.class, SecurityContextExecutorAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).isInstanceOf(BeanCreationException.class);
                });
    }

    @Test
    void shouldRegisterFilterInMainContext_whenManagementPortIsSame() {
        webContextRunner.run(context -> assertThat(
                        context.getBean(FilterRegistrationBean.class).getFilter())
                .isInstanceOf(JwtAuthorizationFilter.class));
    }
}
