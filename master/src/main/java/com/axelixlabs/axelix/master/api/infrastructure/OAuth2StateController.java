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

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.service.auth.CookieService;

import static com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration.OAUTH_LOGIN_PROPERTIES_PREFIX;

/**
 * Generates the {@code state} value for the OAuth2 Authorization Code Flow and stashes it in a cookie, for
 * {@link OAuth2CallbackController} to later verify.
 *
 * @since 28.08.2026
 * @author Nikita Kirillov
 */
@ExternalApiRestController
@ConditionalOnProperty(
        prefix = OAUTH_LOGIN_PROPERTIES_PREFIX,
        name = {"enabled", "state-required"},
        havingValue = "true")
public class OAuth2StateController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int STATE_BYTE_LENGTH = 32;

    private final CookieService cookieService;

    public OAuth2StateController(CookieService cookieService) {
        this.cookieService = cookieService;
    }

    @GetMapping(path = ApiPaths.OAuth2Api.STATE)
    public ResponseEntity<OAuth2StateResponse> state() {
        String state = generateState();

        ResponseCookie cookie = cookieService.buildOAuth2StateCookie(state);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new OAuth2StateResponse(state));
    }

    private static String generateState() {
        byte[] bytes = new byte[STATE_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record OAuth2StateResponse(String state) {}
}
