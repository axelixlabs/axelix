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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.ExternalAuthority;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.jwt.JwtEncoderService;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcTokenProcessor;

/**
 * Controller handling the OAuth2 Authorization Code Flow callback.
 * <p>
 * After the user authenticates with the OIDC provider, the provider redirects
 * to this endpoint with an authorization code.
 * <p>
 * The Authorization Code Flow is defined in RFC 6749 Section 4.1.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1">RFC 6749 Section 4.1</a>
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth"> OpenID Connect Core 1.0 - Authorization Code Flow</a>
 * @since 27.02.2026
 * @author Nikita Kirillov
 */
@ExternalApiRestController
@ConditionalOnProperty(prefix = "axelix.master.auth.oauth2", name = "enabled", havingValue = "true")
@RequestMapping(ApiPaths.OAuth2Api.MAIN)
public class OAuth2CallbackController {

    private static final String WALLBOARD_PATH = "/wallboard";

    private final OidcClient oidcClient;
    private final CookieService cookieService;
    private final OidcTokenProcessor oidcTokenProcessor;
    private final JwtEncoderService jwtEncoderService;

    public OAuth2CallbackController(
            OidcClient oidcClient,
            CookieService cookieService,
            OidcTokenProcessor oidcTokenProcessor,
            JwtEncoderService jwtEncoderService) {
        this.oidcClient = oidcClient;
        this.cookieService = cookieService;
        this.oidcTokenProcessor = oidcTokenProcessor;
        this.jwtEncoderService = jwtEncoderService;
    }

    @GetMapping(path = ApiPaths.OAuth2Api.CALLBACK)
    public ResponseEntity<?> callback(@RequestParam String code) {

        String token = oidcClient.exchangeCodeForIdToken(code);

        String username = oidcTokenProcessor.validateOAuth2JwtTokenAndExtractUsername(token);

        User user = new DefaultUser(
                username,
                "",
                Set.of(new DefaultRole(
                        "ADMIN", Arrays.stream(ExternalAuthority.values()).collect(Collectors.toSet()))));
        String ourToken = jwtEncoderService.generateToken(user);

        ResponseCookie cookie = cookieService.buildAuthCookie(ourToken);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.LOCATION, WALLBOARD_PATH)
                .build();
    }
}
