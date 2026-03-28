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
package com.axelixlabs.axelix.master.filter;

import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Decoder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.OAuth2Properties;
import com.axelixlabs.axelix.master.exception.auth.McpAuthenticationException;
import com.axelixlabs.axelix.master.service.auth.UserLoginService;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;

import static com.axelixlabs.axelix.master.autoconfiguration.web.WebAutoConfigurationConfiguration.API_EXTERNAL_PATH;

/**
 * Filter that authenticates requests to MCP endpoints using either OAuth2 Bearer tokens
 * or Basic Authentication.
 *
 * @author Nikita Kirillov
 */
@Component
public class McpAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(McpAuthenticationFilter.class);

    @Nullable
    private final OidcClient oidcClient;

    @Nullable
    private final UserLoginService userLoginService;

    private final Decoder decoder;

    @Nullable
    private String resourceMetadata = null;

    public McpAuthenticationFilter(
            ObjectProvider<OidcClient> oidcClientProvider,
            ObjectProvider<OAuth2Properties> oAuth2PropertiesProvider,
            ObjectProvider<UserLoginService> userLoginServiceProvider) {
        this.oidcClient = oidcClientProvider.getIfAvailable();
        this.userLoginService = userLoginServiceProvider.getIfAvailable();

        OAuth2Properties oAuth2Properties = oAuth2PropertiesProvider.getIfAvailable();
        if (oAuth2Properties != null) {
            this.resourceMetadata = oAuth2Properties.baseUrl()
                    + API_EXTERNAL_PATH
                    + ApiPaths.McpOAuth2Api.MAIN
                    + ApiPaths.McpOAuth2Api.PROTECTED_RESOURCE_METADATA;
        }

        this.decoder = Base64.getDecoder();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return !requestURI.startsWith("/api/mcp");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            sendUnauthorized(response);
            return;
        }

        if (authHeader.startsWith("Bearer ")) {
            handleBearerAuth(authHeader.substring(7), response, filterChain, request);
        } else if (authHeader.startsWith("Basic ")) {
            handleBasicAuth(authHeader.substring(6), response, filterChain, request);
        } else {
            log.warn(
                    "Unsupported Authorization scheme: '{}'. Expected 'Bearer' or 'Basic'.",
                    authHeader.split(" ")[0]);
            sendForbidden(response, "Unsupported authorization scheme");
        }
    }

    private void handleBearerAuth(
            String token, HttpServletResponse response, FilterChain filterChain, HttpServletRequest request)
            throws IOException {
        if (oidcClient == null) {
            log.warn("Bearer token received but OAuth2 is not configured. Check OAuth2 configuration.");
            sendForbidden(response, "OAuth2 is not configured");
            return;
        }

        try {
            oidcClient.validateTokenViaUserInfoEndpoint(token);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.debug("Bearer token validation failed: {}", e.getMessage());
            sendUnauthorized(response);
        }
    }

    private void handleBasicAuth(
            String base64LoginPassword,
            HttpServletResponse response,
            FilterChain filterChain,
            HttpServletRequest request)
            throws IOException {
        if (userLoginService == null) {
            log.warn("Basic auth received but UserLoginService is unavailable. Check authentication configuration.");
            sendForbidden(response, "Basic auth is not configured");
            return;
        }

        try {
            String[] parts = new String(decoder.decode(base64LoginPassword)).split(":", 2);
            if (parts.length != 2) {
                throw new McpAuthenticationException("Invalid basic auth format");
            }

            // Now we just ignore the token returned from userLoginService.
            userLoginService.login(parts[0], parts[1]);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.debug("Basic auth validation failed: {}", e.getMessage());
            sendForbidden(response, "Invalid username or password");
        }
    }

    private void sendUnauthorized(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Bearer resource_metadata=\"" + resourceMetadata + "\"");
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
