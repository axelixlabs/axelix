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
package com.nucleonforge.axelix.sbs.autoconfiguration.auth;

import io.jsonwebtoken.JwtParser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.common.auth.DefaultJwtDecoderService;
import com.nucleonforge.axelix.common.auth.JwtDecoderService;
import com.nucleonforge.axelix.common.auth.core.JwtAlgorithm;
import com.nucleonforge.axelix.sbs.spring.auth.AuthorityResolver;
import com.nucleonforge.axelix.sbs.spring.auth.Authorizer;
import com.nucleonforge.axelix.sbs.spring.auth.DefaultAuthorityResolver;
import com.nucleonforge.axelix.sbs.spring.auth.DefaultAuthorizer;
import com.nucleonforge.axelix.sbs.spring.auth.JwtAuthorizationFilter;

/**
 * {@link AutoConfiguration} for JWT-based authentication support.
 * <p>
 * This configuration provides default beans for:
 * <ul>
 *   <li>{@link JwtDecoderService} — for decoding and restoring {@code User} objects from tokens.</li>
 *   <li>{@link AuthorityResolver} — to resolve required authorities based on request paths.</li>
 *   <li>{@link Authorizer} — to authorize {@code User} objects against authorization requests.</li>
 *   <li>{@link JwtAuthorizationFilter} — a servlet filter that enforces JWT authorization on incoming requests.</li>
 *   <li>{@link FilterRegistrationBean} for {@link JwtAuthorizationFilter} — registers the filter and restricts it to "/actuator/*" URL pattern.</li>
 * </ul>
 *
 * @author Nikita Kirillov
 * @since 22.07.2025
 */
@AutoConfiguration
@ConditionalOnProperty(name = "axelix.master.auth.jwt")
@ConditionalOnClass({JwtDecoderService.class, JwtParser.class})
public class JwtAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoderService jwtDecoderService(
            final @Value("${axelix.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
            final @Value("${axelix.master.auth.jwt.signing-key}") String signingKey) {
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
