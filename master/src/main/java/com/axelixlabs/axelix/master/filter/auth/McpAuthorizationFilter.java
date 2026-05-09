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
package com.axelixlabs.axelix.master.filter.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.OAuth2Properties;
import com.axelixlabs.axelix.master.autoconfiguration.web.WebAutoConfiguration;
import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.filter.ContentCachingServletRequest;
import com.axelixlabs.axelix.master.filter.FiltersOrder;
import com.axelixlabs.axelix.master.mcp.auth.AuthorizationHeader;
import com.axelixlabs.axelix.master.mcp.auth.McpIdentityAccessManager;

/**
 * Filter that authenticates requests to MCP endpoints using either OAuth2 Bearer tokens
 * or Basic Authentication.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Order(FiltersOrder.MCP_AUTHORIZATION_FILTER)
public class McpAuthorizationFilter extends OncePerRequestFilter {

    public static final String WWW_AUTHENTICATE_OAUTH2_HEADER = "WWW-Authenticate";

    @Nullable
    private final String resourceMetadata;

    private final boolean isOAuth2FlowEnabled;
    private final McpIdentityAccessManager mcpIdentityAccessManager;
    private final SecurityContextExecutor securityContextExecutor;
    private final JwtEncoderService jwtEncoderService;

    public McpAuthorizationFilter(
            ObjectProvider<OAuth2Properties> oAuth2PropertiesProvider,
            McpIdentityAccessManager mcpIdentityAccessManager,
            SecurityContextExecutor securityContextExecutor,
            JwtEncoderService jwtEncoderService) {
        this.mcpIdentityAccessManager = mcpIdentityAccessManager;
        this.securityContextExecutor = securityContextExecutor;
        this.jwtEncoderService = jwtEncoderService;

        OAuth2Properties oAuth2Properties = oAuth2PropertiesProvider.getIfAvailable();

        if (oAuth2Properties != null) {
            this.resourceMetadata = oAuth2Properties.baseUrl()
                    + WebAutoConfiguration.EXTERNAL_API_PATH
                    + ApiPaths.McpOAuth2Api.PROTECTED_RESOURCE_METADATA;
            this.isOAuth2FlowEnabled = true;
        } else {
            this.resourceMetadata = null;
            this.isOAuth2FlowEnabled = false;
        }
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
            throws IOException, ServletException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        AuthorizationHeader authorizationHeader = parseAuthHeader(authHeader);

        if (authorizationHeader == null) {
            handleAuthenticationProblem(response);
            return;
        }

        var wrapper = new ContentCachingServletRequest(request);
        var requestAsString = new String(wrapper.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        try {
            // if nothing is thrown, we expect that all IAM checks passed successfully
            User authenticatedUser = mcpIdentityAccessManager.verifyAccess(requestAsString, authorizationHeader);

            String accessToken = jwtEncoderService.generateToken(authenticatedUser);

            securityContextExecutor.runWithinSecurityContext(
                    () -> filterChain.doFilter(wrapper, response),
                    new DefaultSecurityContext(authenticatedUser, accessToken));
        } catch (AuthorizationException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } catch (AuthenticationException e) {
            handleAuthenticationProblem(response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handleAuthenticationProblem(@NonNull HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // If we received no auth token in a request from the MCP client while having the oauth2 flow enabled,
        // we assume that the AI Agent just tries to access the resource without token for the first time.
        //
        // Note that it does matter whether the Basic auth is also enabled or not. If the Oauth2 flow is enabled
        // alongside the basic auth, the assumption described above still holds.
        if (isOAuth2FlowEnabled) {
            response.setHeader(
                    WWW_AUTHENTICATE_OAUTH2_HEADER,
                    AuthenticationSchemes.BEARER.code() + " resource_metadata=\"" + resourceMetadata + "\"");
        }
    }

    private @Nullable AuthorizationHeader parseAuthHeader(@Nullable String authHeader) {
        if (authHeader == null) {
            return null;
        }

        for (var scheme : Set.of(AuthenticationSchemes.BASIC, AuthenticationSchemes.BEARER)) {

            if (authHeader.startsWith(scheme.prefix())) {
                return new AuthorizationHeader(
                        scheme, authHeader.substring(scheme.prefix().length()));
            }
        }

        return null;
    }
}
