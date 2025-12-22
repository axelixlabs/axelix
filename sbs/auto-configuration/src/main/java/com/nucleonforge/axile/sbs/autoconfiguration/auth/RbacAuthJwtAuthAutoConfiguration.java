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
import com.nucleonforge.axile.common.auth.rbac.filter.AuthorityResolver;
import com.nucleonforge.axile.common.auth.rbac.filter.DefaultAuthorityResolver;
import com.nucleonforge.axile.common.auth.rbac.filter.RbacJwtAuthorizationFilter;
import com.nucleonforge.axile.common.auth.rbac.jwt.service.DefaultRbacJwtDecoderService;
import com.nucleonforge.axile.common.auth.rbac.jwt.service.RbacJwtDecoderService;
import com.nucleonforge.axile.common.auth.rbac.spi.Authorizer;
import com.nucleonforge.axile.common.auth.rbac.spi.DefaultAuthorizer;

/**
 * Auto Configuration for JWT authentication with RBAC Auth support.
 *
 * @author Nikita Kirillov
 * @since 22.07.2025
 */
@AutoConfiguration
@ConditionalOnProperty(name = "axile.sbs.auth.jwt.type", value = "rbac")
@ConditionalOnClass({RbacJwtDecoderService.class, JwtParser.class})
public class RbacAuthJwtAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RbacJwtDecoderService rbacJwtDecoderService(
            final @Value("${axile.sbs.auth.jwt.algorithm}") JwtAlgorithm algorithm,
            final @Value("${axile.sbs.auth.jwt.signing-key}") String signingKey) {
        return new DefaultRbacJwtDecoderService(algorithm, signingKey);
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
    public RbacJwtAuthorizationFilter rbacJwtAuthorizationFilter(
            RbacJwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
        return new RbacJwtAuthorizationFilter(jwtDecoderService, authorityResolver, authorizer);
    }

    @Bean
    public FilterRegistrationBean<RbacJwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
            RbacJwtAuthorizationFilter filter) {
        FilterRegistrationBean<RbacJwtAuthorizationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setName("rbacJwtAuthorizationFilter");
        registration.addUrlPatterns("/actuator/*");
        return registration;
    }
}
