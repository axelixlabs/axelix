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
package com.axelixlabs.axelix.master.api;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.master.api.external.endpoint.OAuth2CallbackController;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.jwt.JwtEncoderService;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link OAuth2CallbackController}.
 *
 * @since 06.03.2026
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axelix.master.auth.options.oauth2.enabled=true",
            "axelix.master.auth.options.oauth2.issuer-uri=http://placeholder.will.be.overridden",
            "axelix.master.auth.options.oauth2.client-id=test-client",
            "axelix.master.auth.options.oauth2.client-secret=test-secret",
            "axelix.master.auth.options.oauth2.redirect-uri=http://localhost:3000/api/external/oauth2/callback"
        })
@TestPropertySource(properties = "axelix.master.auth.static-admin.enabled=true")
class OAuth2CallbackControllerTest {

    private static final String CODE = "test-code";
    private static final String ID_TOKEN = "test-id-token";
    private static final String USERNAME = "test-user";
    private static final String OUR_JWT_TOKEN = "our-jwt-token";
    private static final String AUTHORITIES = "authorities_of_role";

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @MockitoBean
    private OidcClient oidcClient;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private JwtEncoderService jwtEncoderService;

    @BeforeEach
    void prepare() {
        ResponseCookie cookie = ResponseCookie.from("auth-token", OUR_JWT_TOKEN)
                .path("/")
                .httpOnly(true)
                .build();

        ResponseCookie cookie2 =
            ResponseCookie.from("authorities", AUTHORITIES).path("/").build();

        restTemplate = new TestRestTemplate(new RestTemplateBuilder().redirects(HttpRedirects.DONT_FOLLOW));

        when(oidcClient.exchangeCodeForIdToken(CODE)).thenReturn(ID_TOKEN);
        when(oidcClient.validateOAuth2JwtTokenAndExtractUsername(ID_TOKEN)).thenReturn(USERNAME);
        when(jwtEncoderService.generateToken(any())).thenReturn(OUR_JWT_TOKEN);
        when(cookieService.buildAuthCookie(OUR_JWT_TOKEN)).thenReturn(cookie);
        when(cookieService.buildAuthoritiesCookie(any(Set.class))).thenReturn(cookie2);
    }

    @Test
    void shouldRedirectToWallboardWithCookieOnSuccess() {
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation()).hasToString("/wallboard");

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).anyMatch(c -> c.contains("auth-token=" + OUR_JWT_TOKEN));
        assertThat(cookies).anyMatch(c -> c.contains("authorities=" + AUTHORITIES));
    }

    @Test
    void shouldReturn500WhenCodeParamMissing() {
        ResponseEntity<Void> response =
                restTemplate.getForEntity("http://localhost:" + port + "/api/external/oauth2/callback", Void.class);

        // TODO: Are we sure that we need to respond with 500? I think 400 would be a better option
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturn401WhenCodeExchangeFails() {
        when(oidcClient.exchangeCodeForIdToken(CODE)).thenThrow(new OidcTokenExchangeException("exchange failed"));

        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn401WhenTokenValidationFails() {
        when(oidcClient.validateOAuth2JwtTokenAndExtractUsername(ID_TOKEN))
                .thenThrow(new InvalidJwtTokenException("invalid token"));

        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
