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

import tools.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import com.axelixlabs.axelix.common.auth.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.JwtDecoderService;
import com.axelixlabs.axelix.common.utils.Lazy;
import com.axelixlabs.axelix.master.api.external.response.settings.AuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.LoginPasswordAuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.OidcAuthenticationOption;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.OAuth2Properties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.StaticAdminCredentialsProperties;
import com.axelixlabs.axelix.master.filter.CookieBasedJwtAuthorizationFilter;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.DefaultCookieService;
import com.axelixlabs.axelix.master.service.auth.DefaultUserLoginService;
import com.axelixlabs.axelix.master.service.auth.UserLoginService;
import com.axelixlabs.axelix.master.service.auth.jwt.DefaultJwtEncoderService;
import com.axelixlabs.axelix.master.service.auth.jwt.JwtEncoderService;
import com.axelixlabs.axelix.master.service.auth.oauth.DefaultOidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcMetadataProvider;
import com.axelixlabs.axelix.master.service.auth.provider.StaticAdminUserProvider;
import com.axelixlabs.axelix.master.service.auth.provider.UserProvider;

/**
 * Autoconfiguration for security.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class SecurityAutoConfiguration {

    public static final String OAUTH_PROPERTIES_PREFIX = "axelix.master.auth.options.oauth2";
    public static final String STATIC_ADMIN_PROPERTIES_PREFIX = "axelix.master.auth.options.static-admin";

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
    @ConditionalOnProperty(prefix = STATIC_ADMIN_PROPERTIES_PREFIX, name = "enabled", havingValue = "true")
    @EnableConfigurationProperties(StaticAdminCredentialsProperties.class)
    static class StaticCredentialsConfig {

        @Bean
        public AuthenticationOption authSettingsStaticAdmin() {
            return new LoginPasswordAuthenticationOption();
        }

        @Bean
        public UserLoginService userLoginService(JwtEncoderService jwtEncoderService, UserProvider userProvider) {
            return new DefaultUserLoginService(jwtEncoderService, userProvider);
        }

        @Bean
        public StaticAdminUserProvider staticCredentialsUserProvider(
                StaticAdminCredentialsProperties staticCredentialsConfig) {
            return new StaticAdminUserProvider(staticCredentialsConfig);
        }
    }

    /**
     * Autoconfiguration for OAuth2/OIDC security option.
     */
    @AutoConfiguration
    @ConditionalOnProperty(prefix = OAUTH_PROPERTIES_PREFIX, name = "enabled", havingValue = "true")
    @EnableConfigurationProperties(OAuth2Properties.class)
    public static class OAuth2Config {

        @Bean
        public AuthenticationOption authSettingsOAuth2(
                OAuth2Properties oAuth2Properties, OidcMetadataProvider oidcMetadataProvider) {
            return new OidcAuthenticationOption(
                    oAuth2Properties.scopes(),
                    oAuth2Properties.clientId(),
                    oAuth2Properties.redirectUri(),
                    Lazy.of(oidcMetadataProvider::getAuthorizationEndpoint));
        }

        @Bean
        public OidcClient oidcClient(
                RestClient restClient,
                OAuth2Properties oAuth2Properties,
                OidcMetadataProvider oidcMetadataProvider,
                ObjectMapper objectMapper) {
            return new DefaultOidcClient(restClient, oAuth2Properties, oidcMetadataProvider, objectMapper);
        }

        @Bean
        public OidcMetadataProvider oidcMetadataProvider(RestClient restClient, OAuth2Properties oAuth2Properties) {
            return new OidcMetadataProvider(restClient, oAuth2Properties.issuerUri());
        }
    }
}
