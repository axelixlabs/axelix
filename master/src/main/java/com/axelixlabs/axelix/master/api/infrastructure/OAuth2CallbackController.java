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
package com.axelixlabs.axelix.master.api.infrastructure;

import java.time.Instant;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;

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
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcRoleExtractor;
import com.axelixlabs.axelix.master.service.auth.oauth.Tokens;
import com.axelixlabs.axelix.master.service.state.UserService;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;

import static com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration.OAUTH_LOGIN_PROPERTIES_PREFIX;

/**
 * Controller handling the OAuth2 Authorization Code Flow callback.
 * <p>
 * After the user authenticates with the OIDC userOrigin, the userOrigin redirects
 * to this endpoint with an authorization code.
 * <p>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1">RFC 6749 Section 4.1</a>
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth"> OpenID Connect Core 1.0 - Authorization Code Flow</a>
 * @since 27.02.2026
 * @author Nikita Kirillov
 */
@ExternalApiRestController
@ConditionalOnProperty(prefix = OAUTH_LOGIN_PROPERTIES_PREFIX, name = "enabled", havingValue = "true")
public class OAuth2CallbackController {

    private static final String WALLBOARD_PATH = "/wallboard";

    private final OidcClient oidcClient;
    private final CookieService cookieService;
    private final JwtEncoderService jwtEncoderService;
    private final OidcRoleExtractor oidcRoleExtractor;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public OAuth2CallbackController(
            OidcClient oidcClient,
            CookieService cookieService,
            JwtEncoderService jwtEncoderService,
            OidcRoleExtractor oidcRoleExtractor,
            UserService userService,
            ObjectMapper objectMapper) {
        this.oidcClient = oidcClient;
        this.cookieService = cookieService;
        this.jwtEncoderService = jwtEncoderService;
        this.oidcRoleExtractor = oidcRoleExtractor;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @GetMapping(path = ApiPaths.OAuth2Api.CALLBACK)
    public ResponseEntity<?> callback(@RequestParam(required = false) String code) {

        // TODO: Well, the code is actually required, but we have to throw exception manually
        // here instead of relying on spring web binding mechanism to throw it. We need to think about it.
        // The way it is currently implemented (required = false) can confuse the reader
        if (code == null || code.isBlank()) {
            throw new BadRequestException("The authorization code is required");
        }

        Tokens tokens = oidcClient.exchangeCodeForTokens(code);

        String username = oidcClient.validateIdTokenAndExtractUsername(tokens.idToken());

        String userInfoJson = oidcClient.validateAccessTokenAndExtractUserInfo(tokens.accessToken());

        Role role = oidcRoleExtractor.extractRole(userInfoJson);

        User user = new PasswordlessUser(username, Set.of(role));

        upsertUserUpdateLastLoginAt(user, userInfoJson, role);

        String ourToken = jwtEncoderService.generateToken(user);

        ResponseCookie cookie = cookieService.buildAuthCookie(ourToken);
        ResponseCookie cookieAuthorities = cookieService.buildAuthoritiesMetadataCookie(user.getRoles());

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, cookieAuthorities.toString())
                .header(HttpHeaders.LOCATION, WALLBOARD_PATH)
                .build();
    }

    // Always update role & email
    private void upsertUserUpdateLastLoginAt(User user, String userInfoJson, Role role) {
        UserEntity entity = userService.findUserByUsername(user.getUsername()).orElse(null);

        String email = extractEmail(userInfoJson);

        if (entity != null) {
            if (entity.userOrigin() != UserOrigin.OIDC) {
                throw new BadRequestException("OIDC user with username '" + user.getUsername()
                        + "' conflicts with an existing non-OIDC account");
            }
            userService.updateUserPatch(
                    entity.id(), entity.username(), email, null, Set.of(role.getName()), Instant.now());
        } else {
            userService.createFromOidc(user.getUsername(), email, role.getName());
        }
    }

    @Nullable
    private String extractEmail(String userInfoJson) {
        try {
            String email = objectMapper.readTree(userInfoJson).get("email").asString(null);
            return (email == null || email.isBlank()) ? null : email;
        } catch (Exception e) {
            return null;
        }
    }
}
