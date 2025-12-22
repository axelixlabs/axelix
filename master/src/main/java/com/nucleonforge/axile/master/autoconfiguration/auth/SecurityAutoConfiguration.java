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
package com.nucleonforge.axile.master.autoconfiguration.auth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import com.nucleonforge.axile.common.auth.basic.jwt.service.BasicJwtDecoderService;
import com.nucleonforge.axile.common.auth.basic.jwt.service.DefaultBasicJwtDecoderService;
import com.nucleonforge.axile.common.auth.rbac.filter.AuthorityResolver;
import com.nucleonforge.axile.common.auth.rbac.filter.DefaultAuthorityResolver;
import com.nucleonforge.axile.common.auth.rbac.filter.RbacJwtAuthorizationFilter;
import com.nucleonforge.axile.common.auth.rbac.jwt.service.DefaultRbacJwtDecoderService;
import com.nucleonforge.axile.common.auth.rbac.jwt.service.RbacJwtDecoderService;
import com.nucleonforge.axile.common.auth.rbac.spi.Authorizer;
import com.nucleonforge.axile.common.auth.rbac.spi.DefaultAuthorizer;
import com.nucleonforge.axile.master.service.auth.CookieService;
import com.nucleonforge.axile.master.service.auth.DefaultCookieService;
import com.nucleonforge.axile.master.service.auth.jwt.BasicJwtEncoderService;
import com.nucleonforge.axile.master.service.auth.jwt.JwtEncoderService;
import com.nucleonforge.axile.master.service.auth.jwt.RbacJwtEncoderService;
import com.nucleonforge.axile.master.service.auth.provider.StaticAdminUserProvider;

/**
 * Autoconfiguration for security.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class SecurityAutoConfiguration {

    /**
     * Autoconfiguration for {@link JwtProperties}
     */
    @AutoConfiguration
    public static class JwtPropertiesAutoConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axile.master.auth.jwt")
        public JwtProperties jwtProperties() {
            return new JwtProperties();
        }
    }

    /**
     * Autoconfiguration for the JWT-related part, for static-admin security option.
     */
    @AutoConfiguration(after = JwtPropertiesAutoConfiguration.class)
    @ConditionalOnProperty(name = "axile.master.auth.type", havingValue = "static-admin")
    public static class BasicJwtAutoConfiguration {

        @Bean
        public JwtEncoderService jwtEncoderService(JwtProperties jwtProperties) {
            return new BasicJwtEncoderService(
                    jwtProperties.getAlgorithm(), jwtProperties.getSigningKey(), jwtProperties.getLifespan());
        }

        @Bean
        public BasicJwtDecoderService basicJwtDecoderService(JwtProperties jwtProperties) {
            return new DefaultBasicJwtDecoderService(jwtProperties.getAlgorithm(), jwtProperties.getSigningKey());
        }

        /*@Bean
        public BasicJwtAuthorizationFilter basicJwtAuthorizationFilter(
                BasicJwtDecoderService basicAuthJwtDecoderService) {
            return new BasicJwtAuthorizationFilter(basicAuthJwtDecoderService);
        }

        // TODO: rework this filter's path
        @Bean
        public FilterRegistrationBean<RbacJwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
                RbacJwtAuthorizationFilter filter) {
            FilterRegistrationBean<RbacJwtAuthorizationFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(filter);
            registration.setName("jwtAuthorizationFilter");
            registration.addUrlPatterns("/actuator/*");
            return registration;
        }*/
    }

    /**
     * Autoconfiguration for the JWT-related part, for RBAC security option.
     */
    @AutoConfiguration(after = JwtPropertiesAutoConfiguration.class)
    @ConditionalOnProperty(name = "axile.master.auth.type", havingValue = "rbac")
    public static class RbacJwtAutoConfiguration {

        @Bean
        public Authorizer authorizer() {
            return new DefaultAuthorizer();
        }

        @Bean
        public AuthorityResolver authorityResolver() {
            return new DefaultAuthorityResolver();
        }

        @Bean
        public JwtEncoderService jwtEncoderService(JwtProperties jwtProperties) {
            return new RbacJwtEncoderService(
                    jwtProperties.getAlgorithm(), jwtProperties.getSigningKey(), jwtProperties.getLifespan());
        }

        @Bean
        public RbacJwtDecoderService rbacJwtDecoderService(JwtProperties jwtProperties) {
            return new DefaultRbacJwtDecoderService(jwtProperties.getAlgorithm(), jwtProperties.getSigningKey());
        }

        @Bean
        public RbacJwtAuthorizationFilter rbacJwtAuthorizationFilter(
                RbacJwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
            return new RbacJwtAuthorizationFilter(jwtDecoderService, authorityResolver, authorizer);
        }

        // TODO: rework this filter's path in the future
        @Bean
        public FilterRegistrationBean<RbacJwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
                RbacJwtAuthorizationFilter filter) {
            FilterRegistrationBean<RbacJwtAuthorizationFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(filter);
            registration.setName("jwtAuthorizationFilter");
            registration.addUrlPatterns("/actuator/*");
            return registration;
        }
    }

    /**
     * Autoconfiguration for cookie.
     */
    @AutoConfiguration(after = JwtPropertiesAutoConfiguration.class)
    @ConditionalOnProperty(name = "axile.master.auth.cookie.enabled", havingValue = "true", matchIfMissing = true)
    public static class CookieAutoConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axile.master.auth.cookie")
        public CookieProperties cookieProperties() {
            return new CookieProperties();
        }

        @Bean
        public CookieService cookieService(CookieProperties cookieProperties, JwtProperties jwtProperties) {
            return new DefaultCookieService(cookieProperties, jwtProperties);
        }
    }

    /**
     * Autoconfiguration for static-admin security option.
     */
    @AutoConfiguration
    @ConditionalOnProperty(prefix = "axile.master.auth", name = "type", havingValue = "static-admin")
    static class StaticCredentialsConfig {

        private static final String USERNAME_NULL_MESSAGE =
                "The username for the static-admin is 'null'. Make sure the axile.master.auth.static-admin.credentials.username is specified correctly";
        private static final String PASSWORD_NULL_MESSAGE =
                "The password for the static-admin is 'null'. Make sure the axile.master.auth.static-admin.credentials.password is specified correctly";

        @Bean
        public StaticAdminUserProvider staticCredentialsUserProvider(
                StaticAdminCredentialsProperties staticCredentialsConfig) {
            Assert.notNull(staticCredentialsConfig.getUsername(), USERNAME_NULL_MESSAGE);
            Assert.notNull(staticCredentialsConfig.getPassword(), PASSWORD_NULL_MESSAGE);

            return new StaticAdminUserProvider(staticCredentialsConfig);
        }

        @Bean
        @ConfigurationProperties(prefix = "axile.master.auth.static-admin.credentials")
        public StaticAdminCredentialsProperties staticAdminCredentialsProperties() {
            return new StaticAdminCredentialsProperties();
        }
    }
}
