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
package com.axelixlabs.axelix.master.api.external.endpoint.rbac;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.HttpCookieHandling;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.master.api.external.request.LoginRequest;
import com.axelixlabs.axelix.master.api.external.response.CurrentUserResponse;
import com.axelixlabs.axelix.master.api.external.response.RoleProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.UserAuthoritiesResponse;
import com.axelixlabs.axelix.master.api.external.response.UserProfileResponse;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.domain.iam.UserEntity;
import com.axelixlabs.axelix.master.domain.iam.UserStatus;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.auth.jwt.UserJwtEncoderService;
import com.axelixlabs.axelix.master.service.state.auth.UserService;
import com.axelixlabs.axelix.master.testfixtures.TestRoles;
import com.axelixlabs.axelix.master.testfixtures.UserUtils;
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
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axelix.master.auth.options.local.enabled=true",
            "axelix.master.auth.options.super-admin.credentials.username=admin",
            "axelix.master.auth.options.super-admin.credentials.password=admin",
        })
class UserApiTest extends AbstractProtectedEndpointTest {

    private static final String USERS_FEED_PATH = "/api/external/users/feed";
    private static final String CURRENT_USER_PATH = "/api/external/users/me";
    private static final String USER_PROFILE_PATH = "/api/external/users/{userId}/profile";
    private static final String ROLE_USER_COUNTS_PATH = "/api/external/users/role-counts";

    private TestRestTemplate restTemplate;

    @Autowired
    private TestRestTemplateBuilder restTemplateBuilder;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserJwtEncoderService userJwtEncoderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void cleanUsersTable() {
        List<String> ids = userRepository.findAll().stream().map(UserEntity::id).toList();

        if (!ids.isEmpty()) {
            userService.deleteByIds(ids);
        }
    }

    @BeforeEach
    void createRestTemplate(@LocalServerPort int port) {
        restTemplate = new TestRestTemplate(new RestTemplateBuilder()
                .baseUri("http://localhost:" + port)
                .cookieHandling(HttpCookieHandling.ENABLE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/external/users/login", "/api;/external/users/login", "/%61pi/external/users/login"})
    void shouldLoginAsSuperAdmin(String path) {
        // given.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        LoginRequest loginRequest = new LoginRequest("admin", "admin");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                URI.create(restTemplate.getRootUri() + path),
                HttpMethod.POST,
                defaultEntity(loginRequest),
                String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String cookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookieHeader).isNotNull();
        assertThat(cookieHeader).contains(CookieProperties.AUTH_COOKIE_NAME);
        assertThat(cookieHeader)
                .contains(String.valueOf(jwtProperties.lifespan().getSeconds()));
        assertThat(cookieHeader).contains("HttpOnly");
        assertThat(cookieHeader).contains("SameSite=Strict");
        assertSuccessfulCallback(MasterWebEndpoints.LOCAL_LOGIN, UserUtils.withPassword("admin", "admin", Set.of()));
    }

    @Test
    void shouldNotLoginAsSuperAdminWithInvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("admin", "wrongpassword");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertAuthCookiesExpired(response);
        assertAuthenticationFailure(MasterWebEndpoints.LOCAL_LOGIN);
    }

    @Test
    void shouldAuthenticateUserFromDatabase() {
        String username = "db-user";
        String password = "db-password";

        userService.createLocal(username, null, null, "db-user@example.com", password, Set.of("VIEWER"));
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
        assertSuccessfulCallback(MasterWebEndpoints.LOCAL_LOGIN, UserUtils.withPassword(username, password, Set.of()));
    }

    @Test
    void shouldNotAuthenticateUserFromDatabaseWithInvalidCredentials() {
        userService.createLocal(
                "db-user", null, null, "db-user@example.com", "db-password", Set.of(TestRoles.VIEWER.name()));

        LoginRequest loginRequest = new LoginRequest("db-user", "wrong-password");

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertAuthCookiesExpired(response);
        assertAuthenticationFailure(MasterWebEndpoints.LOCAL_LOGIN);
    }

    @Test
    void shouldReturnForbiddenForSuspendedDatabaseUser() {
        // given.
        userService.createLocal("db-user", null, null, "db-user@example.com", "db-password", Set.of("VIEWER"));
        UserEntity user = userRepository.findByUsername("db-user").orElseThrow();
        userService.updateStatusByIds(List.of(user.id()), UserStatus.SUSPENDED);
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
        userService.createLocal(username, null, null, "db-user@example.com", password, Set.of("VIEWER"));
        UserEntity user = userRepository.findByUsername(username).orElseThrow();
        userService.updateStatusByIds(List.of(user.id()), UserStatus.SUSPENDED);
        userService.updateStatusByIds(List.of(user.id()), UserStatus.ACTIVE);
        LoginRequest loginRequest = new LoginRequest(username, password);

        // when.
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/external/users/login", HttpMethod.POST, defaultEntity(loginRequest), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);
        assertThat(userRepository.findById(user.id()).orElseThrow().lastLoginAt())
                .isNotNull();
        assertSuccessfulCallback(MasterWebEndpoints.LOCAL_LOGIN, UserUtils.withPassword(username, password, Set.of()));
    }

    @Test
    void shouldClearCookieOnLogout() {
        PasswordlessUser actor = UserUtils.passwordless("someUser", Set.of());
        String token = userJwtEncoderService.generateToken(actor);

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
        ResponseEntity<String> logoutResponse =
                restTemplate.exchange("/api/external/users/logout", HttpMethod.POST, logoutEntity, String.class);

        // then.
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertAuthenticationFailure(MasterWebEndpoints.LOGOUT);
    }

    @Test
    void shouldExpireAuthoritiesCookieWhenAuthCookieIsMissing() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, CookieProperties.AUTHORITIES_COOKIE_NAME + "=W1VTRVJTX1JFQURd");

        // when.
        ResponseEntity<String> response =
                restTemplate.exchange(USERS_FEED_PATH, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertAuthCookiesExpired(response);
    }

    private static void assertAuthCookiesExpired(ResponseEntity<String> response) {
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE))
                .containsExactlyInAnyOrder(
                        CookieProperties.AUTH_COOKIE_NAME
                                + "=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; HttpOnly; SameSite=Strict",
                        CookieProperties.AUTHORITIES_COOKIE_NAME
                                + "=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Strict");
    }

    @Test
    void shouldReturnAllManagedUsers() {
        // given.
        userService.createLocal("alice", "Alice", "Smith", "alice@example.com", "aliceSecret", Set.of("ADMIN"));
        UserEntity alice = userRepository.findByUsername("alice").orElseThrow();

        userService.createFromOidc("bob", "Bob", null, "bob@example.com", "hash-bob", "VIEWER");
        UserEntity bob = userRepository.findByUsername("bob").orElseThrow();

        // language=json
        String expectedFeed = """
                [
                  {
                    "id": "%s",
                    "username": "alice",
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "email": "alice@example.com",
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
                    "roles": ["VIEWER"],
                    "userOrigin": "OIDC",
                    "status": "ACTIVE",
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
    void shouldReturnCurrentLocalUser() {
        // given.
        userService.createLocal("alice", "Alice", "Smith", null, "secret", Set.of("VIEWER"));
        loginAs("alice", "secret");

        // when.
        var response = restTemplate.getForEntity(CURRENT_USER_PATH, CurrentUserResponse.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var profile = response.getBody();
        assertThat(profile.username()).isEqualTo("alice");
        assertThat(profile.firstName()).isEqualTo("Alice");
        assertThat(profile.lastName()).isEqualTo("Smith");
        assertThat(profile.roles()).containsExactly("VIEWER");
    }

    @Test
    void shouldReturnCurrentSuperAdmin() {
        // given.
        loginAs("admin", "admin");

        // when.
        var response = restTemplate.getForEntity(CURRENT_USER_PATH, CurrentUserResponse.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var profile = response.getBody();
        assertThat(profile.username()).isEqualTo("admin");
        assertThat(profile.firstName()).isNull();
        assertThat(profile.lastName()).isNull();
        assertThat(profile.roles()).containsExactly("SUPER_ADMIN");
    }

    @Test
    void shouldReturnUserProfile() {
        // given.
        userService.createLocal("alice", "Alice", "Smith", "alice@example.com", "aliceSecret", Set.of("ADMIN"));
        UserEntity alice = userRepository.findByUsername("alice").orElseThrow();
        userService.updateStatusByIds(List.of(alice.id()), UserStatus.SUSPENDED);

        // language=json
        String expectedUser = """
                {
                  "id": "%s",
                  "username": "alice",
                  "firstName": "Alice",
                  "lastName": "Smith",
                  "email": "alice@example.com",
                  "roles": ["ADMIN"],
                  "userOrigin": "LOCAL",
                  "status": "SUSPENDED",
                  "lastLoginAt": null
                }
                """.formatted(alice.id());

        // when.
        IdentityAwareTestRestTemplate superAdmin = restTemplateBuilder.asUsersFeedViewer();
        ResponseEntity<String> response = superAdmin.getForEntity(USER_PROFILE_PATH, String.class, alice.id());

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).node("user").isEqualTo(expectedUser);
        assertThatJson(response.getBody()).node("roles").isArray().isNotEmpty();
        assertThatJson(response.getBody())
                .node("authorities.effectiveAuthorities")
                .isArray()
                .isNotEmpty();
        assertThatJson(response.getBody())
                .node("authorities.ungrantedAuthorities")
                .isArray()
                .hasSize(2);
        assertThat(response.getBody()).doesNotContain("password");
        assertSuccessfulCallback(MasterWebEndpoints.USER_PROFILE, superAdmin.getActor());
    }

    @Test
    void shouldReturnNotFoundIfUserIsNotFound() {
        // given.
        String unknownUserId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<String> response =
                restTemplateBuilder.asUsersFeedViewer().getForEntity(USER_PROFILE_PATH, String.class, unknownUserId);

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

    @Test
    void shouldReturnUserCountsByRole() {
        // given.
        userService.createLocal(
                "alice", "Alice", "Smith", "alice@example.com", "aliceSecret", Set.of("ADMIN", "EDITOR"));
        userService.createFromOidc("bob", "Bob", null, "bob@example.com", "hash-bob", "VIEWER");

        // language=json
        String expectedCounts = """
                [
                  { "role": "ADMIN", "usersCount": 1 },
                  { "role": "EDITOR", "usersCount": 1 },
                  { "role": "VIEWER", "usersCount": 1 }
                ]
                """;

        // when.
        IdentityAwareTestRestTemplate viewer = restTemplateBuilder.asUsersFeedViewer();
        ResponseEntity<String> response = viewer.getForEntity(ROLE_USER_COUNTS_PATH, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).isEqualTo(expectedCounts);
        assertSuccessfulCallback(MasterWebEndpoints.USERS_READ_ROLE_COUNTS, viewer.getActor());
    }

    @Test
    void shouldReturnUserAuthorityPartition() {
        // given.
        String username = "authority-feed-" + UUID.randomUUID();
        userService.createLocal(username, null, null, null, "password", Set.of("EDITOR"));
        String userId = userRepository.findByUsername(username).orElseThrow().id();
        IdentityAwareTestRestTemplate viewer = restTemplateBuilder.asUsersFeedViewer();

        try {
            // when.
            ResponseEntity<UserProfileResponse> response =
                    viewer.getForEntity(USER_PROFILE_PATH, UserProfileResponse.class, userId);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThat(response.getBody().user().id()).isEqualTo(userId);
            assertThat(response.getBody().roles())
                    .extracting(RoleProfileResponse::name)
                    .contains("ADMIN", "EDITOR", "VIEWER");
            assertThat(response.getBody().authorities().effectiveAuthorities())
                    .extracting(UserAuthoritiesResponse.AuthorityProfileResponse::codename)
                    .isSorted()
                    .containsExactly(
                            "caches:clear", "caches:toggle", "gc:manage", "quartz:write", "scheduled-tasks:write");
            assertThat(response.getBody().authorities().ungrantedAuthorities())
                    .extracting(UserAuthoritiesResponse.AuthorityProfileResponse::codename)
                    .isSorted()
                    .containsExactly(
                            "config-properties.values:read",
                            "environment.values:read",
                            "heap-dump:read",
                            "users:manage",
                            "users:read");
            assertSuccessfulCallback(MasterWebEndpoints.USER_PROFILE, viewer.getActor());
        } finally {
            userService.deleteByIds(List.of(userId));
        }
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(
                new TestableMasterWebEndpoint(MasterWebEndpoints.CURRENT_USER, CURRENT_USER_PATH),
                new TestableMasterWebEndpoint(MasterWebEndpoints.USERS_READ, USERS_FEED_PATH),
                new TestableMasterWebEndpoint(MasterWebEndpoints.USER_PROFILE, "/api/external/users/missing/profile"),
                new TestableMasterWebEndpoint(MasterWebEndpoints.USERS_READ_ROLE_COUNTS, ROLE_USER_COUNTS_PATH));
    }

    private void loginAs(String username, String password) {
        restTemplate.postForEntity(
                "/api/external/users/login", defaultEntity(new LoginRequest(username, password)), Void.class);
    }

    private HttpEntity<String> defaultEntity(LoginRequest loginRequest) {
        String requestBody = objectMapper.writeValueAsString(loginRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }
}
