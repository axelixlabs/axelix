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
import org.springframework.boot.http.client.HttpCookieHandling;
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

import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.testfixtures.TestRoles;
import com.axelixlabs.axelix.master.api.external.request.LoginRequest;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserStatus;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.auth.UserService;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

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
 * @author Ilya Naumov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(
        properties = {
            "axelix.master.auth.options.local.enabled=true",
            "axelix.master.auth.options.super-admin.credentials.username=admin",
            "axelix.master.auth.options.super-admin.credentials.password=admin",
        })
class UserApiTest extends AbstractProtectedEndpointTest {

    private static final String USERS_FEED_PATH = "/api/external/users/feed";
    private static final String USER_BY_ID_PATH = "/api/external/users/feed/{userId}";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestRestTemplateBuilder restTemplateBuilder;

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
        userRepository.findAll().forEach(user -> userService.deleteById(user.id()));
    }

    @BeforeEach
    void statelessRestTemplate() {
        // The auto-configured TestRestTemplate defaults to ENABLE_WHEN_POSSIBLE cookie handling, so once an
        // Apache HttpClient is on the classpath (pulled in transitively by spring-cloud-config) it starts
        // persisting the auth cookie set by login-like requests across subsequent calls. That leaks a valid
        // cookie into tests that intentionally send none (e.g. logout without a cookie). Disable cookie
        // handling so every request is stateless and carries only the cookies it explicitly sets.
        restTemplate = restTemplate.withCookieHandling(HttpCookieHandling.DISABLE);
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
        assertThat(cookieHeader).contains(CookieProperties.AUTH_COOKIE_NAME);
        assertThat(cookieHeader)
                .contains(String.valueOf(jwtProperties.lifespan().getSeconds()));
        assertThat(cookieHeader).contains("HttpOnly");
        assertThat(cookieHeader).contains("SameSite=Strict");
        assertSuccessfulCallback(MasterWebEndpoints.LOCAL_LOGIN, new DefaultUser("admin", "admin", Set.of()));
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
        assertAuthenticationFailure(MasterWebEndpoints.LOCAL_LOGIN);
    }

    @Test
    void shouldAuthenticateUserFromDatabase() {
        String username = "db-user";
        String password = "db-password";

        userService.createLocal(username, null, null, "db-user@example.com", null, null, password, "VIEWER");
        UserEntity user = userRepository.findByUsername(username).orElseThrow();

        LoginRequest loginRequest = new LoginRequest(username, password);

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);

        UserEntity updated = userRepository.findById(user.id()).orElseThrow();
        assertThat(updated.lastLoginAt()).isNotNull();
        assertSuccessfulCallback(MasterWebEndpoints.LOCAL_LOGIN, new DefaultUser(username, password, Set.of()));
    }

    @Test
    void shouldNotAuthenticateUserFromDatabaseWithInvalidCredentials() {
        userService.createLocal(
                "db-user", null, null, "db-user@example.com", null, null, "db-password", TestRoles.VIEWER.getName());

        LoginRequest loginRequest = new LoginRequest("db-user", "wrong-password");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNullOrEmpty();
        assertAuthenticationFailure(MasterWebEndpoints.LOCAL_LOGIN);
    }

    @Test
    void shouldReturnForbiddenForSuspendedDatabaseUser() {
        // given.
        userService.createLocal("db-user", null, null, "db-user@example.com", null, null, "db-password", "VIEWER");
        UserEntity user = userRepository.findByUsername("db-user").orElseThrow();
        userService.updateStatus(user.id(), UserStatus.SUSPENDED);
        LoginRequest loginRequest = new LoginRequest("db-user", "db-password");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("USER_SUSPENDED");
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNullOrEmpty();
        assertThat(userRepository.findById(user.id()).orElseThrow().lastLoginAt())
                .isNull();
        assertAccessDenied(MasterWebEndpoints.LOCAL_LOGIN);
    }

    @Test
    void shouldAuthenticateReactivatedDatabaseUser() {
        // given.
        String username = "db-user";
        String password = "db-password";

        // and.
        userService.createLocal(username, null, null, "db-user@example.com", null, null, password, "VIEWER");
        UserEntity user = userRepository.findByUsername(username).orElseThrow();
        userService.updateStatus(user.id(), UserStatus.SUSPENDED);
        userService.updateStatus(user.id(), UserStatus.ACTIVE);
        LoginRequest loginRequest = new LoginRequest(username, password);

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);
        assertThat(userRepository.findById(user.id()).orElseThrow().lastLoginAt())
                .isNotNull();
        assertSuccessfulCallback(MasterWebEndpoints.LOCAL_LOGIN, new DefaultUser(username, password, Set.of()));
    }

    @Test
    void shouldClearCookieOnLogout() {
        PasswordlessUser actor = new PasswordlessUser("someUser", Set.of());
        String token = jwtEncoderService.generateToken(actor);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, CookieProperties.AUTH_COOKIE_NAME + "=" + token);

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
                .anySatisfy(cookieHeader -> assertThat(cookieHeader).contains(CookieProperties.AUTH_COOKIE_NAME))
                .anySatisfy(
                        cookieHeader -> assertThat(cookieHeader).contains(CookieProperties.AUTHORITIES_COOKIE_NAME));
        assertSuccessfulCallback(MasterWebEndpoints.LOGOUT, actor);
    }

    @Test
    void shouldReturn401OnLogoutWithoutCookie() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> logoutEntity = new HttpEntity<>(headers);

        // when.
        ResponseEntity<String> logoutResponse = restTemplateBuilder
                .withoutToken()
                .exchange("/api/external/users/logout", HttpMethod.POST, logoutEntity, String.class);

        // then.
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertAuthenticationFailure(MasterWebEndpoints.LOGOUT);
    }

    @Test
    void shouldReturnAllManagedUsers() {
        // given.
        userService.createLocal("alice", "Alice", "Smith", "alice@example.com", null, null, "aliceSecret", "ADMIN");
        UserEntity alice = userRepository.findByUsername("alice").orElseThrow();

        userService.createFromOidc("bob", "Bob", null, "bob@example.com", null, null, "VIEWER");
        UserEntity bob = userRepository.findByUsername("bob").orElseThrow();
        userService.updateStatus(bob.id(), UserStatus.SUSPENDED);

        // language=json
        String expectedFeed = """
                [
                  {
                    "id": "%s",
                    "username": "alice",
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "email": "alice@example.com",
                    "jobTitle": null,
                    "organizationalUnit": null,
                    "roles": ["ADMIN"],
                    "userOrigin": "LOCAL",
                    "status": "ACTIVE",
                    "lastLoginAt": null
                  },
                  {
                    "id": "%s",
                    "username": "bob",
                    "firstName": "Bob",
                    "lastName": null,
                    "email": "bob@example.com",
                    "jobTitle": null,
                    "organizationalUnit": null,
                    "roles": ["VIEWER"],
                    "userOrigin": "OAUTH2/OIDC",
                    "status": "SUSPENDED",
                    "lastLoginAt": "${json-unit.any-string}"
                  }
                ]
                """.formatted(alice.id(), bob.id());

        // when.
        IdentityAwareTestRestTemplate superAdmin = restTemplateBuilder.asUsersFeedViewer();
        ResponseEntity<String> response = superAdmin.getForEntity(USERS_FEED_PATH, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(expectedFeed);
        assertThat(response.getBody()).doesNotContain("password");
        assertSuccessfulCallback(MasterWebEndpoints.USERS_READ, superAdmin.getActor());
    }

    @Test
    void shouldReturnUserByHisId() {
        // given.
        userService.createLocal(
                "alice",
                "Alice",
                "Smith",
                "alice@example.com",
                "Engineering Manager",
                "Engineering",
                "aliceSecret",
                "ADMIN");
        UserEntity alice = userRepository.findByUsername("alice").orElseThrow();
        userService.updateStatus(alice.id(), UserStatus.SUSPENDED);

        // language=json
        String expectedUser = """
                {
                  "id": "%s",
                  "username": "alice",
                  "firstName": "Alice",
                  "lastName": "Smith",
                  "email": "alice@example.com",
                  "jobTitle": "Engineering Manager",
                  "organizationalUnit": "Engineering",
                  "roles": ["ADMIN"],
                  "userOrigin": "LOCAL",
                  "status": "SUSPENDED",
                  "lastLoginAt": null
                }
                """.formatted(alice.id());

        // when.
        IdentityAwareTestRestTemplate superAdmin = restTemplateBuilder.asUsersFeedViewer();
        ResponseEntity<String> response = superAdmin.getForEntity(USER_BY_ID_PATH, String.class, alice.id());

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).isEqualTo(expectedUser);
        assertThat(response.getBody()).doesNotContain("password");
        assertSuccessfulCallback(MasterWebEndpoints.USER_READ_ONE, superAdmin.getActor());
    }

    @Test
    void shouldReturnNotFoundIfUserIsNotFound() {
        // given.
        String unknownUserId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<String> response =
                restTemplateBuilder.asUsersFeedViewer().getForEntity(USER_BY_ID_PATH, String.class, unknownUserId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnEmptyUsersFeed() {
        // when.
        IdentityAwareTestRestTemplate superAdmin = restTemplateBuilder.asUsersFeedViewer();
        ResponseEntity<String> response = superAdmin.getForEntity(USERS_FEED_PATH, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).isEqualTo("[]");
        assertSuccessfulCallback(MasterWebEndpoints.USERS_READ, superAdmin.getActor());
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(new TestableMasterWebEndpoint(MasterWebEndpoints.USERS_READ, USERS_FEED_PATH));
    }

    private HttpEntity<String> defaultEntity(LoginRequest loginRequest) {
        String requestBody = objectMapper.writeValueAsString(loginRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }
}
