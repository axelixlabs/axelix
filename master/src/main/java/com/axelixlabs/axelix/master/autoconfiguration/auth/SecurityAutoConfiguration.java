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

import java.util.List;

import com.axelixlabs.axelix.master.autoconfiguration.mcp.ConditionalOnMcpServerEnabled;
import tools.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

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
import com.axelixlabs.axelix.common.utils.Lazy;
import com.axelixlabs.axelix.master.api.external.response.settings.AuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.LocalAuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.OidcAuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.SuperAdminAuthenticationOption;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.OAuth2Properties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.SuperAdminConfigurationProperties;
import com.axelixlabs.axelix.master.filter.auth.CookieBasedJwtAuthorizationFilter;
import com.axelixlabs.axelix.master.mcp.auth.handler.BasicMcpAuthenticationHandler;
import com.axelixlabs.axelix.master.mcp.auth.handler.BearerMcpAuthenticationHandler;
import com.axelixlabs.axelix.master.mcp.auth.handler.McpAuthenticationHandler;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.DefaultCookieService;
import com.axelixlabs.axelix.master.service.auth.MasterAuthorityResolver;
import com.axelixlabs.axelix.master.service.auth.oauth.DefaultOidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.JmesPathOidcRoleExtractor;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcMetadataProvider;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcRoleExtractor;
import com.axelixlabs.axelix.master.service.auth.provider.CompositeUserAuthenticator;
import com.axelixlabs.axelix.master.service.auth.provider.DatabaseUserAuthenticator;
import com.axelixlabs.axelix.master.service.auth.provider.SuperAdminUserAuthenticator;
import com.axelixlabs.axelix.master.service.auth.provider.UserAuthenticator;
import com.axelixlabs.axelix.master.service.state.UserService;

/**
 * Autoconfiguration for security.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@AutoConfiguration
public class SecurityAutoConfiguration {

    public static final String OAUTH_LOGIN_PROPERTIES_PREFIX = "axelix.master.auth.options.oauth2";
    public static final String SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX = "axelix.master.auth.options.super-admin";
    public static final String LOCAL_LOGIN_PROPERTIES_PREFIX = "axelix.master.auth.options.local";

    @Bean
    public MasterAuthorityResolver masterAuthorityResolver() {
        return new MasterAuthorityResolver();
    }

    @Bean
    public WebIdentityAccessManager identityAccessManager(
            JwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
        return new DefaultWebIdentityAccessManager(jwtDecoderService, authorityResolver, authorizer);
    }

    @Bean
    public Authorizer authorizer() {
        return new DefaultAuthorizer();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public UserAuthenticator compositeUserAuthenticator(List<UserAuthenticator> userAuthenticators) {
        return new CompositeUserAuthenticator(userAuthenticators);
    }

    @Bean
    @ConditionalOnMcpServerEnabled
    public McpAuthenticationHandler basicAuthMcpAuthenticationHandler(UserAuthenticator userAuthenticator) {
        return new BasicMcpAuthenticationHandler(userAuthenticator);
    }

    /**
     * Autoconfiguration for the JWT-related part.
     */
    @AutoConfiguration
    public static class JwtAutoConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axelix.master.auth.jwt")
        public JwtProperties jwtProperties() {
            return new JwtProperties();
        }

        @Bean
        public JwtEncoderService jwtEncoderService(JwtProperties jwtProperties) {
            return new DefaultJwtEncoderService(
                    jwtProperties.getAlgorithm(), jwtProperties.getSigningKey(), jwtProperties.getLifespan());
        }

        @Bean
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
                WebIdentityAccessManager webIdentityAccessManager,
                CookieProperties cookieProperties,
                SecurityContextExecutor securityContextExecutor) {
            return new CookieBasedJwtAuthorizationFilter(
                    cookieProperties.getName(), webIdentityAccessManager, securityContextExecutor);
        }
    }

    /**
     * Autoconfiguration for {@link LocalAuthenticationOption}.
     */
    @AutoConfiguration
    @ConditionalOnProperty(prefix = LOCAL_LOGIN_PROPERTIES_PREFIX, name = "enabled", havingValue = "true")
    public static class LocalLoginAutoConfiguration {

        @Bean
        public AuthenticationOption localAuthenticationOption() {
            return new LocalAuthenticationOption();
        }

        @Bean
        public DatabaseUserAuthenticator databaseUserAuthenticator(
                UserService userService, PasswordEncoder passwordEncoder) {
            return new DatabaseUserAuthenticator(userService, passwordEncoder);
        }
    }

    /**
     * Autoconfiguration for {@link SuperAdminAuthenticationOption}.
     */
    @AutoConfiguration
    @EnableConfigurationProperties(SuperAdminConfigurationProperties.class)
    public static class SuperAdminLoginAutoConfiguration {

        @Bean
        public AuthenticationOption superAdminAuthenticationOption() {
            return new SuperAdminAuthenticationOption();
        }

        @Bean
        public SuperAdminUserAuthenticator staticCredentialsUserAuthenticator(
                SuperAdminConfigurationProperties staticCredentialsConfig) {
            return new SuperAdminUserAuthenticator(staticCredentialsConfig);
        }
    }

    /**
     * Autoconfiguration for OAuth2/OIDC security option.
     */
    @AutoConfiguration
    @ConditionalOnProperty(prefix = OAUTH_LOGIN_PROPERTIES_PREFIX, name = "enabled", havingValue = "true")
    @EnableConfigurationProperties(OAuth2Properties.class)
    public static class OAuth2LoginAutoConfiguration {

        @Bean
        @ConditionalOnMcpServerEnabled
        public McpAuthenticationHandler bearerMcpAuthenticationHandler(OidcRoleExtractor oidcRoleExtractor) {
            return new BearerMcpAuthenticationHandler(oidcRoleExtractor);
        }

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

        @Bean
        public OidcRoleExtractor oidcRoleExtractor(OidcClient oidcClient, OAuth2Properties oAuth2Properties) {
            return new JmesPathOidcRoleExtractor(oidcClient, oAuth2Properties.roleAttributePath());
        }
    }
}
