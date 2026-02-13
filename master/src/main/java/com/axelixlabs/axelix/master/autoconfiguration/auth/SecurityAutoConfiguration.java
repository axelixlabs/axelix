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
package com.axelixlabs.axelix.master.autoconfiguration.auth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import com.axelixlabs.axelix.common.auth.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.JwtDecoderService;
import com.axelixlabs.axelix.master.filter.CookieBasedJwtAuthorizationFilter;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.DefaultCookieService;
import com.axelixlabs.axelix.master.service.auth.jwt.DefaultJwtEncoderService;
import com.axelixlabs.axelix.master.service.auth.jwt.JwtEncoderService;
import com.axelixlabs.axelix.master.service.auth.provider.StaticAdminUserProvider;

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
