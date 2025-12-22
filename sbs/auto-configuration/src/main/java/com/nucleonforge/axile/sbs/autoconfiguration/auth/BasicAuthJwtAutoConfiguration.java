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
import com.nucleonforge.axile.common.auth.basic.filter.BasicJwtAuthorizationFilter;
import com.nucleonforge.axile.common.auth.basic.jwt.service.BasicJwtDecoderService;
import com.nucleonforge.axile.common.auth.basic.jwt.service.DefaultBasicJwtDecoderService;

/**
 * Auto Configuration for JWT authentication with Basic Auth support.
 *
 * @since 19.12.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
@ConditionalOnProperty(name = "axile.sbs.auth.jwt.type", value = "static-admin")
@ConditionalOnClass({BasicJwtDecoderService.class, JwtParser.class})
public class BasicAuthJwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BasicJwtDecoderService basicJwtDecoderService(
            final @Value("${axile.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
            final @Value("${axile.master.auth.jwt.signing-key}") String signingKey) {
        return new DefaultBasicJwtDecoderService(algorithm, signingKey);
    }

    @Bean
    @ConditionalOnMissingBean
    public BasicJwtAuthorizationFilter basicJwtAuthorizationFilter(BasicJwtDecoderService jwtDecoderService) {
        return new BasicJwtAuthorizationFilter(jwtDecoderService);
    }

    @Bean
    public FilterRegistrationBean<BasicJwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
            BasicJwtAuthorizationFilter filter) {
        FilterRegistrationBean<BasicJwtAuthorizationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setName("jwtAuthorizationFilter");
        registration.addUrlPatterns("/actuator/*");
        return registration;
    }
}
