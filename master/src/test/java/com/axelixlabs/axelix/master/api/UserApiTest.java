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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.api.external.endpoint.UserApi;
import com.axelixlabs.axelix.master.api.external.request.LoginRequest;
import com.axelixlabs.axelix.master.autoconfiguration.auth.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.JwtProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link UserApi}.
 *
 * @since 22.12.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CookieProperties cookieProperties;

    @Autowired
    private JwtProperties jwtProperties;

    @Value("${test-tokens.valid-token}")
    private String validToken;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_shouldReturnJwtInCookie() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        LoginRequest loginRequest = new LoginRequest("admin", "admin");
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/api/external/users/login", HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String cookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookieHeader).isNotNull();
        assertThat(cookieHeader).contains(cookieProperties.getName());
        assertThat(cookieHeader)
                .contains(String.valueOf(jwtProperties.getLifespan().getSeconds()));
        assertThat(cookieHeader).contains("HttpOnly");
        assertThat(cookieHeader).contains("SameSite=Strict");
    }

    @Test
    void login_withInvalidCredentials() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        LoginRequest loginRequest = new LoginRequest("admin", "wrongpassword");
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/api/external/users/login", HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        String cookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookieHeader).isNull();
    }

    @Test
    void logout_shouldClearCookie() {
        String token = validToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, cookieProperties.getName() + "=" + token);

        HttpEntity<Void> logoutEntity = new HttpEntity<>(headers);

        ResponseEntity<String> logoutResponse =
                restTemplate.exchange("/api/external/users/logout", HttpMethod.POST, logoutEntity, String.class);

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String logoutCookieHeader = logoutResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(logoutCookieHeader).isNotNull();
        assertThat(logoutCookieHeader).contains(cookieProperties.getName());
        assertThat(logoutCookieHeader.toLowerCase()).contains("max-age=0");
    }

    @Test
    void logout_withoutCookie() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> logoutEntity = new HttpEntity<>(headers);

        ResponseEntity<String> logoutResponse =
                restTemplate.exchange("/api/external/users/logout", HttpMethod.POST, logoutEntity, String.class);

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
