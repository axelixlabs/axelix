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

import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcRoleExtractor;
import com.axelixlabs.axelix.master.service.auth.oauth.Tokens;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;

/**
 * Controller handling the OAuth2 Authorization Code Flow callback.
 * <p>
 * After the user authenticates with the OIDC provider, the provider redirects
 * to this endpoint with an authorization code.
 * <p>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1">RFC 6749 Section 4.1</a>
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth"> OpenID Connect Core 1.0 - Authorization Code Flow</a>
 * @since 27.02.2026
 * @author Nikita Kirillov
 */
@ExternalApiRestController
@ConditionalOnProperty(prefix = "axelix.master.auth.options.oauth2", name = "enabled", havingValue = "true")
public class OAuth2CallbackController {

    private static final String WALLBOARD_PATH = "/wallboard";

    private final OidcClient oidcClient;
    private final CookieService cookieService;
    private final JwtEncoderService jwtEncoderService;
    private final OidcRoleExtractor oidcRoleExtractor;

    public OAuth2CallbackController(
            OidcClient oidcClient,
            CookieService cookieService,
            JwtEncoderService jwtEncoderService,
            OidcRoleExtractor oidcRoleExtractor) {
        this.oidcClient = oidcClient;
        this.cookieService = cookieService;
        this.jwtEncoderService = jwtEncoderService;
        this.oidcRoleExtractor = oidcRoleExtractor;
    }

    @GetMapping(path = ApiPaths.OAuth2Api.CALLBACK)
    public ResponseEntity<?> callback(@RequestParam(required = false) String code) {

        // TODO: handle it better
        if (code == null) {
            throw new BadRequestException("The authorization code is required");
        }

        Tokens tokens = oidcClient.exchangeCodeForTokens(code);

        String username = oidcClient.validateIdTokenAndExtractUsername(tokens.idToken());

        Role role = oidcRoleExtractor.extractRole(tokens);

        User user = new PasswordlessUser(username, Set.of(role));

        String ourToken = jwtEncoderService.generateToken(user);

        ResponseCookie cookie = cookieService.buildAuthCookie(ourToken);
        ResponseCookie cookieAuthorities = cookieService.buildAuthoritiesMetadataCookie(user.getRoles());

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, cookieAuthorities.toString())
                .header(HttpHeaders.LOCATION, WALLBOARD_PATH)
                .build();
    }
}
