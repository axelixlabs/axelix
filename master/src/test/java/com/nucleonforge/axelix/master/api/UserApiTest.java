/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.master.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nucleonforge.axelix.master.api.request.LoginRequest;
import com.nucleonforge.axelix.master.autoconfiguration.auth.CookieProperties;
import com.nucleonforge.axelix.master.autoconfiguration.auth.JwtProperties;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link com.nucleonforge.axelix.master.api.UserApi}.
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
                restTemplate.exchange("/api/axelix/users/login", HttpMethod.POST, request, String.class);

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
            restTemplate.exchange("/api/axelix/users/login", HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        String cookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookieHeader).isNull();
    }

    @Test
    void logout_shouldClearCookie() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTc1NDUwMjA4MCwiZXhwIjozMzMxMzAyMDgwLCJyb2xlcyI6W3sibmFtZSI6IlJPTEVfVVNFUiIsImF1dGhvcml0aWVzIjpbIkVOViIsIklORk8iXSwiY29tcG9uZW50cyI6W119LHsibmFtZSI6IlJPTEVfRU5HSU5FRVIiLCJhdXRob3JpdGllcyI6WyJCRUFOUyIsIkhFQUxUSCJdLCJjb21wb25lbnRzIjpbXX1dfQ.xLuprlFUgeslSlZEp1Btf20EjQ8w_-Dx3V0RM2xUjq-PU48iiXCZ2BT4EzzOCferYB9wCxV-LyX3DObiqg_X-A";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, cookieProperties.getName() + "=" + token);

        HttpEntity<Void> logoutEntity = new HttpEntity<>(headers);

        ResponseEntity<String> logoutResponse =
            restTemplate.exchange("/api/axelix/users/logout", HttpMethod.POST, logoutEntity, String.class);

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
            restTemplate.exchange("/api/axelix/users/logout", HttpMethod.POST, logoutEntity, String.class);

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
