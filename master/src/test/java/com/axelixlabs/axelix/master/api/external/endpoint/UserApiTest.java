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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.master.api.external.request.LoginRequest;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.state.UserService;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.common.auth.core.DefaultAuthority.USERS_VIEW;
import static com.axelixlabs.axelix.common.auth.core.DefaultRole.SUPER_ADMIN;
import static com.axelixlabs.axelix.common.domain.http.HttpMethod.GET;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
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
            "axelix.master.auth.options.local.enabled=true",
            "axelix.master.auth.options.super-admin.credentials.username=admin",
            "axelix.master.auth.options.super-admin.credentials.password=admin",
        })
class UserApiTest {

    private static final String USERS_FEED_PATH = "/api/external/users/feed";
    private static final String USER_BY_ID_PATH = "/api/external/users/feed/{userId}";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestRestTemplateBuilder restTemplateBuilder;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @BeforeEach
    void cleanUsersTable() {
        userRepository.deleteAll();
    }

    @Test
    void shouldLoginAsSuperAdmin() {
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
        assertThat(cookieHeader).contains(cookieProperties.getAuthCookieName());
        assertThat(cookieHeader)
                .contains(String.valueOf(jwtProperties.getLifespan().getSeconds()));
        assertThat(cookieHeader).contains("HttpOnly");
        assertThat(cookieHeader).contains("SameSite=Strict");
    }

    @Test
    void shouldNotLoginAsSuperAdminWithInvalidCredentials() {
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
    void shouldAuthenticateUserFromDatabase() {
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
    void shouldNotAuthenticateUserFromDatabaseWithInvalidCredentials() {
        userService.create(
                "db-user", "db-user@example.com", "db-password", DefaultRole.VIEWER.getName(), UserOrigin.LOCAL);

        LoginRequest loginRequest = new LoginRequest("db-user", "wrong-password");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNullOrEmpty();
    }

    @Test
    void shouldClearCookieOnLogout() {
        String token = jwtEncoderService.generateToken(new PasswordlessUser("someUser", Set.of()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, cookieProperties.getAuthCookieName() + "=" + token);

        HttpEntity<Void> logoutEntity = new HttpEntity<>(headers);

        // when.
        ResponseEntity<String> logoutResponse =
                restTemplate.exchange("/api/external/users/logout", HttpMethod.POST, logoutEntity, String.class);

        // then.
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(logoutResponse.getHeaders().get(HttpHeaders.SET_COOKIE))
                .hasSize(2)
                .allSatisfy(cookieHeader -> {
                    assertThat(cookieHeader.toLowerCase()).contains("max-age=0");
                });
        assertThat(logoutResponse.getHeaders().get(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookieHeader -> assertThat(cookieHeader).contains(cookieProperties.getAuthCookieName()))
                .anySatisfy(
                        cookieHeader -> assertThat(cookieHeader).contains(cookieProperties.getAuthoritiesCookieName()));
    }

    @Test
    void shouldReturn401OnLogoutWithoutCookie() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> logoutEntity = new HttpEntity<>(headers);

        // when.
        ResponseEntity<String> logoutResponse =
                restTemplate.exchange("/api/external/users/logout", HttpMethod.POST, logoutEntity, String.class);

        // then.
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnAllManagedUsers() {
        UserEntity alice = insertUser("alice", "alice@example.com", "aliceSecret", Set.of("ADMIN"), UserOrigin.LOCAL);
        UserEntity bob = insertUser("bob", "bob@example.com", null, Set.of("VIEWER"), UserOrigin.OIDC);

        // language=json
        String expectedFeed = """
                [
                  {
                    "id": "%s",
                    "username": "alice",
                    "email": "alice@example.com",
                    "roles": ["ADMIN"],
                    "userOrigin": "LOCAL",
                    "lastLoginAt": null
                  },
                  {
                    "id": "%s",
                    "username": "bob",
                    "email": "bob@example.com",
                    "roles": ["VIEWER"],
                    "userOrigin": "OAUTH2/OIDC",
                    "lastLoginAt": null
                  }
                ]
                """.formatted(alice.id(), bob.id());

        // when.
        ResponseEntity<String> response =
                restTemplateBuilder.withRole(SUPER_ADMIN).getForEntity(USERS_FEED_PATH, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(expectedFeed);
        assertThat(response.getBody()).doesNotContain("password");
    }

    @Test
    void shouldReturnUserByHisId() {
        // given.
        UserEntity alice = insertUser("alice", "alice@example.com", "aliceSecret", Set.of("ADMIN"), UserOrigin.LOCAL);

        // language=json
        String expectedUser = """
                {
                  "id": "%s",
                  "username": "alice",
                  "email": "alice@example.com",
                  "roles": ["ADMIN"],
                  "userOrigin": "LOCAL",
                  "lastLoginAt": null
                }
                """.formatted(alice.id());

        // when.
        ResponseEntity<String> response =
                restTemplateBuilder.withRole(SUPER_ADMIN).getForEntity(USER_BY_ID_PATH, String.class, alice.id());

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).isEqualTo(expectedUser);
        assertThat(response.getBody()).doesNotContain("password");
    }

    @Test
    void shouldReturnNotFoundIfUserIsNotFound() {
        // given.
        String unknownUserId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<String> response =
                restTemplateBuilder.withRole(SUPER_ADMIN).getForEntity(USER_BY_ID_PATH, String.class, unknownUserId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnEmptyUsersFeed() {
        // when.
        ResponseEntity<String> response =
                restTemplateBuilder.withRole(SUPER_ADMIN).getForEntity(USERS_FEED_PATH, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).isEqualTo("[]");
    }

    @ProtectedEndpointTests(method = GET, path = USERS_FEED_PATH, requiredAuthority = USERS_VIEW)
    void negativeAuthTestsOnGetUsersFeed() {}

    private HttpEntity<String> defaultEntity(LoginRequest loginRequest) {
        String requestBody = objectMapper.writeValueAsString(loginRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }

    private UserEntity insertUser(
            String username, String email, String password, Set<String> roles, UserOrigin provider) {
        UserEntity entity = new UserEntity(
                UUID.randomUUID().toString(),
                username,
                email,
                password == null ? null : passwordEncoder.encode(password),
                new UserEntity.Roles(roles),
                provider,
                null);
        return jdbcAggregateTemplate.insert(entity);
    }
}
