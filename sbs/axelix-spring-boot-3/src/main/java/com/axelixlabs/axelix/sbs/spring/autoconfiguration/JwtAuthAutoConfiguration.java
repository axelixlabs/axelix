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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.JwtDecoderService;
import com.axelixlabs.axelix.sbs.spring.core.auth.AuthorityResolver;
import com.axelixlabs.axelix.sbs.spring.core.auth.Authorizer;
import com.axelixlabs.axelix.sbs.spring.core.auth.DefaultAuthorizer;
import com.axelixlabs.axelix.sbs.spring.core.auth.DefaultSecurityManager;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthorizationFilter;
import com.axelixlabs.axelix.sbs.spring.core.auth.PassthroughAuthorityResolver;
import com.axelixlabs.axelix.sbs.spring.core.auth.SecurityManager;
import com.axelixlabs.axelix.sbs.spring.core.config.AuthConfigurationProperties;

/**
 * {@link AutoConfiguration} for JWT-based authentication support.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @since 22.07.2025
 */
@AutoConfiguration
@EnableConfigurationProperties(AuthConfigurationProperties.class)
public class JwtAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoderService jwtDecoderService(AuthConfigurationProperties configurationProperties) {
        return new DefaultJwtDecoderService(
                configurationProperties.getJwt().getAlgorithm(),
                configurationProperties.getJwt().getSigningKey());
    }

    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("removal") // TODO: https://github.com/axelixlabs/axelix/issues/757
    public AuthorityResolver authorityResolver() {
        return new PassthroughAuthorityResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public Authorizer authorizer() {
        return new DefaultAuthorizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityManager securityManager(
            JwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
        return new DefaultSecurityManager(jwtDecoderService, authorityResolver, authorizer);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
            SecurityManager securityManager) {

        var jwtAuthorizationFilter = new JwtAuthorizationFilter(securityManager);
        var registration = new FilterRegistrationBean<>(jwtAuthorizationFilter);
        registration.setName("jwtAuthorizationFilter");
        return registration;
    }
}
