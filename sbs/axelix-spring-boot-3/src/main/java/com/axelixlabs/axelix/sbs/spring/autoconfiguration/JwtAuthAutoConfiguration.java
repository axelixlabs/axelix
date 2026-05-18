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

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.service.AuthorityResolver;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.auth.service.DefaultAuthorizer;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.DefaultWebIdentityAccessManager;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.WebIdentityAccessManager;
import com.axelixlabs.axelix.sbs.spring.core.auth.DefaultAuthorityResolver;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthorizationFilter;
import com.axelixlabs.axelix.sbs.spring.core.auth.ThreadLocalSecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.config.AuthProperties;
import com.axelixlabs.axelix.sbs.spring.core.validate.ValidationListener;

/**
 * {@link AutoConfiguration} for JWT-based authentication support.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @since 22.07.2025
 */
@AutoConfiguration
@EnableConfigurationProperties(WebEndpointProperties.class)
public class JwtAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ValidationListener validationListener() {
        return new ValidationListener();
    }

    @Bean
    @ConfigurationProperties(prefix = "axelix.sbs.auth")
    public AuthProperties authProperties() {
        return new AuthProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoderService jwtDecoderService(AuthProperties authProperties) {
        return new DefaultJwtDecoderService(
                authProperties.getJwt().getAlgorithm(), authProperties.getJwt().getSigningKey());
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtEncoderService jwtEncoderService(AuthProperties authProperties) {
        return new DefaultJwtEncoderService(
                authProperties.getJwt().getAlgorithm(),
                authProperties.getJwt().getSigningKey(),
                authProperties.getJwt().getDuration());
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorityResolver authorityResolver() {
        return new DefaultAuthorityResolver((pathTemplate, actualPath) -> {
            PathPattern parse = new PathPatternParser().parse(pathTemplate);
            return parse.matchAndExtract(PathContainer.parsePath(actualPath)) != null;
        });
    }

    @Bean
    @ConditionalOnMissingBean
    public Authorizer authorizer() {
        return new DefaultAuthorizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebIdentityAccessManager webIdentityAccessManager(
            JwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
        return new DefaultWebIdentityAccessManager(jwtDecoderService, authorityResolver, authorizer);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityContextExecutor securityContextExecutor() {
        return new ThreadLocalSecurityContextExecutor();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
            WebIdentityAccessManager webIdentityAccessManager,
            SecurityContextExecutor securityContextExecutor,
            WebEndpointProperties webEndpointProperties) {

        var jwtAuthorizationFilter = new JwtAuthorizationFilter(
                webIdentityAccessManager, securityContextExecutor, webEndpointProperties.getBasePath());
        var registration = new FilterRegistrationBean<>(jwtAuthorizationFilter);
        registration.setName("jwtAuthorizationFilter");
        return registration;
    }
}
