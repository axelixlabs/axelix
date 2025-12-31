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
package com.nucleonforge.axelix.master.autoconfiguration.auth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import com.nucleonforge.axelix.common.auth.DefaultJwtDecoderService;
import com.nucleonforge.axelix.common.auth.JwtDecoderService;
import com.nucleonforge.axelix.master.service.auth.CookieService;
import com.nucleonforge.axelix.master.service.auth.DefaultCookieService;
import com.nucleonforge.axelix.master.service.auth.jwt.DefaultJwtEncoderService;
import com.nucleonforge.axelix.master.service.auth.jwt.JwtEncoderService;
import com.nucleonforge.axelix.master.service.auth.provider.StaticAdminUserProvider;

/**
 * Autoconfiguration for security.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class SecurityAutoConfiguration {

    /**
     * Autoconfiguration for the JWT-related part.
     */
    @AutoConfiguration
    public static class JwtAutoConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axelix.master.auth.jwt")
        JwtProperties jwtProperties() {
            return new JwtProperties();
        }

        @Bean
        JwtEncoderService jwtEncoderService(JwtProperties jwtProperties) {
            return new DefaultJwtEncoderService(
                    jwtProperties.getAlgorithm(), jwtProperties.getSigningKey(), jwtProperties.getLifespan());
        }

        @Bean
        @ConditionalOnMissingBean
        public JwtDecoderService jwtDecoderService(JwtProperties jwtProperties) {
            return new DefaultJwtDecoderService(jwtProperties.getAlgorithm(), jwtProperties.getSigningKey());
        }
    }

    /**
     * Autoconfiguration for cookie.
     */
    @AutoConfiguration(after = JwtAutoConfiguration.class)
    @ConditionalOnProperty(
            prefix = "axelix.master.auth.cookie",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public static class CookieAutoConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axelix.master.auth.cookie")
        public CookieProperties cookieProperties() {
            return new CookieProperties();
        }

        @Bean
        public CookieService cookieService(CookieProperties cookieProperties, JwtProperties jwtProperties) {
            return new DefaultCookieService(cookieProperties, jwtProperties);
        }

        @Bean
        public CookieBasedJwtAuthorizationFilter cookieBasedJwtAuthorizationFilter(
                JwtDecoderService jwtDecoderService, CookieProperties cookieProperties) {
            return new CookieBasedJwtAuthorizationFilter(jwtDecoderService, cookieProperties.getName());
        }
    }

    /**
     * Autoconfiguration for static-admin security option.
     */
    @AutoConfiguration
    @ConditionalOnProperty(prefix = "axelix.master.auth", name = "type", havingValue = "static-admin")
    static class StaticCredentialsConfig {

        private static final String USERNAME_NULL_MESSAGE =
                "The username for the static-admin is 'null'. Make sure the axelix.master.auth.static-admin.credentials.username is specified correctly";
        private static final String PASSWORD_NULL_MESSAGE =
                "The password for the static-admin is 'null'. Make sure the axelix.master.auth.static-admin.credentials.password is specified correctly";

        @Bean
        public StaticAdminUserProvider staticCredentialsUserProvider(
                StaticAdminCredentialsProperties staticCredentialsConfig) {
            Assert.notNull(staticCredentialsConfig.getUsername(), USERNAME_NULL_MESSAGE);
            Assert.notNull(staticCredentialsConfig.getPassword(), PASSWORD_NULL_MESSAGE);

            return new StaticAdminUserProvider(staticCredentialsConfig);
        }

        @Bean
        @ConfigurationProperties(prefix = "axelix.master.auth.static-admin.credentials")
        public StaticAdminCredentialsProperties staticAdminCredentialsProperties() {
            return new StaticAdminCredentialsProperties();
        }
    }
}
