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
package com.nucleonforge.axile.sbs.autoconfiguration.auth;

import io.jsonwebtoken.JwtParser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.common.auth.JwtAlgorithm;
import com.nucleonforge.axile.common.auth.spi.Authorizer;
import com.nucleonforge.axile.common.auth.spi.DefaultAuthorizer;
import com.nucleonforge.axile.common.auth.spi.jwt.service.DefaultJwtDecoderService;
import com.nucleonforge.axile.common.auth.spi.jwt.service.JwtDecoderService;
import com.nucleonforge.axile.common.auth.spi.verification.AuthorityResolver;
import com.nucleonforge.axile.common.auth.spi.verification.DefaultAuthorityResolver;
import com.nucleonforge.axile.sbs.auth.filter.JwtAuthorizationFilter;

/**
 * Auto Configuration for JWT-based authentication support.
 *
 * @author Nikita Kirillov
 * @since 22.07.2025
 */
@AutoConfiguration
@ConditionalOnProperty(name = "axile.master.auth.jwt")
@ConditionalOnClass({JwtDecoderService.class, JwtParser.class})
public class JwtAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoderService jwtDecoderService(
            final @Value("${axile.sbs.auth.jwt.algorithm}") JwtAlgorithm algorithm,
            final @Value("${axile.sbs.auth.jwt.signing-key}") String signingKey) {
        return new DefaultJwtDecoderService(algorithm, signingKey);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorityResolver authorityResolver() {
        return new DefaultAuthorityResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public Authorizer authorizer() {
        return new DefaultAuthorizer();
    }

    @Bean
    @ConditionalOnMissingBean
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
