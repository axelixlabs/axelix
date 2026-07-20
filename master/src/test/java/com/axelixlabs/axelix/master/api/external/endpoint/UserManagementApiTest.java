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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.common.auth.core.DefaultAuthority.USERS_MANAGEMENT;
import static com.axelixlabs.axelix.common.auth.core.DefaultRole.SUPER_ADMIN;
import static com.axelixlabs.axelix.common.domain.http.HttpMethod.DELETE;
import static com.axelixlabs.axelix.common.domain.http.HttpMethod.POST;
import static com.axelixlabs.axelix.common.domain.http.HttpMethod.PUT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link UserManagementApi}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "axelix.master.auth.options.local.enabled=true")
public class UserManagementApiTest {

    private static final String USERS_CREATE_PATH = "/api/external/users-management/create";
    private static final String USERS_DELETE_PATH = "/api/external/users-management/delete";
    private static final String USERS_UPDATE_PATH = "/api/external/users-management/update";

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUsersTable() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() {
        // language=json
        String request = """
                {
                  "username": "newUser",
                  "email": "newUser@example.com",
                  "password": "plainPassword",
                  "role": "EDITOR"
                }
                """;

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_CREATE_PATH, HttpMethod.POST, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        List<UserEntity> users = userRepository.findAll();
        assertThat(users).hasSize(1);

        UserEntity saved = users.getFirst();
        assertThat(saved.id()).isNotBlank();
        assertThat(saved.username()).isEqualTo("newUser");
        assertThat(saved.email()).isEqualTo("newUser@example.com");
        assertThat(saved.roles().values()).containsExactly("EDITOR");
        assertThat(saved.userOrigin()).isEqualTo(UserOrigin.LOCAL);
        assertThat(saved.lastLoginAt()).isNull();
        assertThat(saved.password()).isNotEqualTo("plainPassword"); // Hash password
        assertThat(passwordEncoder.matches("plainPassword", saved.password())).isTrue();
    }

    @Test
    void shouldReturnBadRequest_WhenCreateRequestRoleIsNull() {
        // language=json
        String request = """
                {
                  "username": "u",
                  "email": "u@example.com",
                  "password": "p",
                  "role": null
                }
                """;

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_CREATE_PATH, HttpMethod.POST, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnBadRequest_WhenCreateRequestRoleIsBlank() {
        // language=json
        String request = """
                {
                  "username": "u",
                  "email": "u@example.com",
                  "password": "p",
                  "role": "   "
                }
                """;

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_CREATE_PATH, HttpMethod.POST, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnBadRequest_WhenCreateRequestRoleIsSuperAdmin() {
        // language=json
        String request = """
                {
                  "username": "u",
                  "email": "u@example.com",
                  "password": "p",
                  "role": "  super_admin  "
                }
                """;

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_CREATE_PATH, HttpMethod.POST, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnBadRequest_WhenCreateRequestRoleIsUnknown() {
        // language=json
        String request = """
                {
                  "username": "u",
                  "email": "u@example.com",
                  "password": "p",
                  "role": "NOT_A_REAL_ROLE"
                }
                """;

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_CREATE_PATH, HttpMethod.POST, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnBadRequest_WhenCreateRequestUsernameIsDuplicate() {
        insertUser("existingUser", "existing@example.com", "p", Set.of("VIEWER"), UserOrigin.LOCAL);

        // language=json
        String request = """
                {
                  "username": "existingUser",
                  "email": "other@example.com",
                  "password": "p",
                  "role": "VIEWER"
                }
                """;

        // when.
        ResponseEntity<String> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_CREATE_PATH, HttpMethod.POST, defaultEntity(request), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("USERNAME_ALREADY_EXISTS");
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldReturnBadRequest_WhenCreateRequestEmailIsDuplicate() {
        insertUser("user_test", "user_test@example.com", "p", Set.of("VIEWER"), UserOrigin.LOCAL);

        // language=json
        String request = """
                {
                  "username": "existingUser",
                  "email": "user_test@example.com",
                  "password": "p",
                  "role": "VIEWER"
                }
                """;

        // when.
        ResponseEntity<String> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_CREATE_PATH, HttpMethod.POST, defaultEntity(request), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("EMAIL_ALREADY_EXISTS");
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldDeleteUser() {
        UserEntity user = insertUser("toDelete", "d@example.com", "p", Set.of("VIEWER"), UserOrigin.LOCAL);

        // language=json
        String request = """
                {
                  "id":"%s"
                }
                """.formatted(user.id());

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_DELETE_PATH, HttpMethod.DELETE, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.findById(user.id())).isEmpty();
    }

    @Test
    void shouldUpdateAllUserFields() {
        // given.
        UserEntity user = insertUser("oldName", "old@example.com", "oldPass", Set.of("VIEWER"), UserOrigin.LOCAL);

        // language=json
        String request = """
                {
                  "id": "%s",
                  "username": "newName",
                  "email": "new@example.com",
                  "roles": ["ADMIN", "EDITOR"],
                  "password": "newPass"
                }
                """.formatted(user.id());

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_UPDATE_PATH, HttpMethod.PUT, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        UserEntity updated = userRepository.findById(user.id()).orElseThrow();
        assertThat(updated.username()).isEqualTo("newName");
        assertThat(updated.email()).isEqualTo("new@example.com");
        assertThat(updated.roles().values()).containsExactlyInAnyOrder("ADMIN", "EDITOR");
        assertThat(passwordEncoder.matches("newPass", updated.password())).isTrue();
    }

    @Test
    void shouldUpdateRetainingThePassword_WhenUpdateRequestContainsNullPassword() {
        String oldPassword = "oldPass";
        UserEntity user =
                insertUser("oldUsername", "old-email@example.com", oldPassword, Set.of("VIEWER"), UserOrigin.LOCAL);

        String newUsername = "newUsername";

        // language=json
        String request = """
                {
                  "id": "%s",
                  "username": "%s",
                  "email": null,
                  "roles": ["VIEWER"],
                  "password": null
                }
                """.formatted(user.id(), newUsername);

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_UPDATE_PATH, HttpMethod.PUT, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        UserEntity updated = userRepository.findById(user.id()).orElseThrow();
        assertThat(updated.username()).isEqualTo(newUsername);
        assertThat(updated.email()).isEqualTo(null);
        assertThat(updated.roles().values()).isEqualTo(user.roles().values());
        assertThat(passwordEncoder.matches(oldPassword, updated.password())).isTrue();
    }

    @Test
    void shouldReturnBadRequest_WhenUpdateRolesContainSuperAdmin() {
        UserEntity user = insertUser("u", "u@example.com", "p", Set.of("VIEWER"), UserOrigin.LOCAL);

        // language=json
        String request = """
                {
                  "id": "%s",
                  "username": "u",
                  "roles": ["VIEWER", "  SUPER_ADMIN "]
                }
                """.formatted(user.id());

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_UPDATE_PATH, HttpMethod.PUT, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        UserEntity untouched = userRepository.findById(user.id()).orElseThrow();
        assertThat(untouched.roles().values()).containsExactly("VIEWER");
    }

    @Test
    void shouldReturnBadRequest_WhenCreateRequestRoleIsMissing() {
        // language=json
        String request = """
                {
                  "username": "u",
                  "email": "u@example.com",
                  "password": "p"
                }
                """;

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_CREATE_PATH, HttpMethod.POST, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnBadRequest_WhenUpdateRolesAreEmpty() {
        UserEntity user = insertUser("u", "u@example.com", "p", Set.of("VIEWER"), UserOrigin.LOCAL);

        // language=json
        String request = """
                {
                  "id": "%s",
                  "username": "u",
                  "roles": []
                }
                """.formatted(user.id());

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_UPDATE_PATH, HttpMethod.PUT, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        UserEntity untouched = userRepository.findById(user.id()).orElseThrow();
        assertThat(untouched.roles().values()).containsExactly("VIEWER");
    }

    @Test
    void shouldReturnBadRequest_WhenUpdateRolesContainBlank() {
        UserEntity user = insertUser("u", "u@example.com", "p", Set.of("VIEWER"), UserOrigin.LOCAL);

        // language=json
        String request = """
                {
                  "id": "%s",
                  "username": "u",
                  "roles": ["VIEWER", "   "]
                }
                """.formatted(user.id());

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_UPDATE_PATH, HttpMethod.PUT, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequest_WhenUpdateRolesContainUnknownRole() {
        UserEntity user = insertUser("u", "u@example.com", "p", Set.of("VIEWER"), UserOrigin.LOCAL);

        // language=json
        String request = """
                {
                  "id": "%s",
                  "username": "u",
                  "roles": ["NOT_A_REAL_ROLE"]
                }
                """.formatted(user.id());

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRole(SUPER_ADMIN)
                .exchange(USERS_UPDATE_PATH, HttpMethod.PUT, defaultEntity(request), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ProtectedEndpointTests(
            method = POST,
            path = USERS_CREATE_PATH,
            requiredAuthority = USERS_MANAGEMENT,
            // language=json
            jsonBody = """
                    {
                        "username": "x",
                        "email": "x@example.com",
                        "password": "p",
                        "role":"VIEWER"
                    }
                    """)
    void negativeAuthTestsOnCreateUser() {}

    @ProtectedEndpointTests(
            method = DELETE,
            path = USERS_DELETE_PATH,
            requiredAuthority = USERS_MANAGEMENT,
            // language=json
            jsonBody = """
                    {
                      "id": "some-id"
                    }
                    """)
    void negativeAuthTestsOnDeleteUser() {}

    @ProtectedEndpointTests(
            method = PUT,
            path = USERS_UPDATE_PATH,
            requiredAuthority = USERS_MANAGEMENT,
            // language=json
            jsonBody = """
                    {
                      "id": "some-id",
                      "username": "x",
                      "password": null,
                      "roles": ["VIEWER"]
                    }
                    """)
    void negativeAuthTestsOnUpdateUser() {}

    private HttpEntity<String> defaultEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
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
