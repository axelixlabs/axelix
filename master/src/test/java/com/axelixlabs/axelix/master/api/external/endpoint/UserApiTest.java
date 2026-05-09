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

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.master.api.external.request.LoginRequest;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.state.UserService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link UserApi}.
 *
 * @since 22.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(
        properties = {
            "axelix.master.auth.options.static-admin.enabled=true",
            "axelix.master.auth.options.static-admin.credentials.username=admin",
            "axelix.master.auth.options.static-admin.credentials.password=admin"
        })
class UserApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CookieProperties cookieProperties;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtEncoderService jwtEncoderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void cleanUsersTable() {
        userRepository.deleteAll();
    }

    @Test
    void login_shouldReturnJwtInCookie() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        LoginRequest loginRequest = new LoginRequest("admin", "admin");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
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
    void login_withInvalidCredentials() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        LoginRequest loginRequest = new LoginRequest("admin", "wrongpassword");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        String cookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookieHeader).isNull();
    }

    @Test
    void login_shouldAuthenticateUserFromDatabase() {
        userService.create("db-user", "db-user@example.com", "db-password", "VIEWER", UserOrigin.LOCAL);
        UserEntity user = userRepository.findByUsername("db-user").orElseThrow();

        LoginRequest loginRequest = new LoginRequest("db-user", "db-password");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);

        UserEntity updated = userRepository.findById(user.id()).orElseThrow();
        assertThat(updated.lastLoginAt()).isNotNull();
    }

    @Test
    void login_shouldAuthenticateStaticAdmin_WhenDatabaseUserHasSameUsername_ButStaticAdminPasswordMatches() {
        userService.create("admin", "db-admin@example.com", "db-password", "VIEWER", UserOrigin.LOCAL);
        UserEntity user = userRepository.findByUsername("admin").orElseThrow();

        LoginRequest loginRequest = new LoginRequest("admin", "admin");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);

        UserEntity unchanged = userRepository.findById(user.id()).orElseThrow();
        assertThat(unchanged.lastLoginAt()).isNull();
    }

    @Test
    void
            login_shouldAuthenticateDatabaseUser_WhenDatabaseUserHasSameUsernameAsStaticAdmin_ButDatabasePasswordMatches() {
        userService.create("admin", "db-admin@example.com", "db-password", "VIEWER", UserOrigin.LOCAL);
        UserEntity user = userRepository.findByUsername("admin").orElseThrow();

        LoginRequest loginRequest = new LoginRequest("admin", "db-password");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);

        UserEntity updated = userRepository.findById(user.id()).orElseThrow();
        assertThat(updated.lastLoginAt()).isNotNull();
    }

    @Test
    void login_shouldReturnUnauthorized_WhenStaticAdminAndDatabaseUserShareUsername_ButPasswordMatchesNeither() {
        userService.create("admin", "db-admin@example.com", "db-password", "VIEWER", UserOrigin.LOCAL);
        UserEntity user = userRepository.findByUsername("admin").orElseThrow();

        LoginRequest loginRequest = new LoginRequest("admin", "wrong-password");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNull();

        UserEntity unchanged = userRepository.findById(user.id()).orElseThrow();
        assertThat(unchanged.lastLoginAt()).isNull();
    }

    @Test
    void login_shouldReturnUnauthorized_WhenUserDoesNotExistInDatabaseAndProperties() {
        LoginRequest loginRequest = new LoginRequest("missing-user", "missing-password");
        ;

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    void login_shouldReturnAuthoritiesMetadataCookie() {
        LoginRequest loginRequest = new LoginRequest("admin", "admin");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<String> cookieHeaders = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookieHeaders).isNotNull();
        assertThat(cookieHeaders).hasSize(2);
        assertThat(cookieHeaders)
                .anySatisfy(cookieHeader -> assertThat(cookieHeader).contains(cookieProperties.getName()));
        assertThat(cookieHeaders)
                .anySatisfy(cookieHeader -> assertThat(cookieHeader).contains(cookieProperties.getNameAuthority()));
    }

    @Test
    void logout_shouldClearCookie() {
        String token = jwtEncoderService.generateToken(new PasswordlessUser("someUser", Set.of()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, cookieProperties.getName() + "=" + token);

        HttpEntity<Void> logoutEntity = new HttpEntity<>(headers);

        // when.
        ResponseEntity<String> logoutResponse =
                restTemplate.exchange("/api/external/users/logout", HttpMethod.POST, logoutEntity, String.class);

        // then.
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

        // when.
        ResponseEntity<String> logoutResponse =
                restTemplate.exchange("/api/external/users/logout", HttpMethod.POST, logoutEntity, String.class);

        // then.
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private HttpEntity<String> defaultEntity(LoginRequest loginRequest) {
        String requestBody = objectMapper.writeValueAsString(loginRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }
}
