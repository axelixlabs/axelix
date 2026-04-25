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
package com.axelixlabs.axelix.master.service.state.users;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.exception.auth.UserInvalidValueException;
import com.axelixlabs.axelix.master.exception.auth.UserRoleNotFoundException;
import com.axelixlabs.axelix.master.exception.auth.UserWithIdNotFoundException;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.Provider;
import com.axelixlabs.axelix.master.service.state.DatabaseUserService;
import com.axelixlabs.axelix.master.service.state.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Base class for integration tests of {@link DatabaseUserService}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
abstract class DatabaseUserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    @AfterEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void create_shouldPersistUser() {
        // when.
        userService.create("alice", "alice@example.com", "plainPass", "ADMIN", Provider.LOCAL);

        // then.
        List<UserEntity> users = userRepository.findAll();
        assertThat(users).hasSize(1);

        UserEntity saved = users.get(0);
        assertThat(saved.id()).isNotBlank();
        assertThat(saved.username()).isEqualTo("alice");
        assertThat(saved.email()).isEqualTo("alice@example.com");
        assertThat(saved.roles().values()).containsExactly("ADMIN");
        assertThat(saved.provider()).isEqualTo(Provider.LOCAL);
        assertThat(saved.password()).isNotEqualTo("plainPass"); // Hash password
        assertThat(passwordEncoder.matches("plainPass", saved.password())).isTrue();
        assertThat(saved.lastLoginAt()).isNull();
    }

    @Test
    void create_shouldAllowNullEmailAndPassword() {
        // when.
        userService.create("bob", null, null, "VIEWER", Provider.OIDC);

        // then.
        UserEntity saved = userRepository.findByUsername("bob").orElseThrow();
        assertThat(saved.email()).isNull();
        assertThat(saved.password()).isNull();
        assertThat(saved.provider()).isEqualTo(Provider.OIDC);
    }

    @Test
    void create_shouldThrowWhenRoleIsNotAllowed() {
        // when.
        assertThatThrownBy(() -> userService.create("alice", "alice@example.com", "p", "SUPER_ADMIN", Provider.LOCAL))
                // then.
                .isInstanceOf(UserRoleNotFoundException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void create_shouldThrowWhenRoleDoesNotExist() {
        // when.
        assertThatThrownBy(() -> userService.create("alice", "alice@example.com", "p", "NOT_A_ROLE", Provider.LOCAL))
                // then.
                .isInstanceOf(UserRoleNotFoundException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void create_shouldThrowWhenUsernameIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.create("   ", "alice@example.com", "p", "VIEWER", Provider.LOCAL))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void create_shouldThrowWhenEmailIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.create("alice", "   ", "p", "VIEWER", Provider.LOCAL))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void create_shouldThrowWhenPasswordIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.create("alice", "alice@example.com", "   ", "VIEWER", Provider.LOCAL))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void create_shouldThrowWhenRoleIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.create("alice", "alice@example.com", "p", "   ", Provider.LOCAL))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void delete_shouldRemoveUser() {
        userService.create("alice", "alice@example.com", "p", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.delete(existing.id());

        // then.
        assertThat(userRepository.findById(existing.id())).isEmpty();
    }

    @Test
    void delete_shouldBeNoOpWhenUserDoesNotExist() {
        // when.
        assertThatCode(() -> userService.delete("non-existent-id")).doesNotThrowAnyException();
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);
        userService.create("bob", "b@example.com", "p", "ADMIN", Provider.OIDC);

        // when.
        List<UserEntity> all = userService.getAll();

        // then.
        assertThat(all).extracting(UserEntity::username).containsExactlyInAnyOrder("alice", "bob");
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoUsers() {
        // when. / then.
        assertThat(userService.getAll()).isEmpty();
    }

    @Test
    void getUserByUsername_shouldReturnMatchingUser() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);

        // when.
        Optional<UserEntity> found = userService.getUserByUsername("alice");

        // then.
        assertThat(found).isPresent();
        assertThat(found.get().username()).isEqualTo("alice");
        assertThat(found.get().email()).isEqualTo("a@example.com");
        assertThat(found.get().provider()).isEqualTo(Provider.LOCAL);
    }

    @Test
    void getUserByUsername_shouldReturnEmptyWhenNotFound() {
        // when. / then.
        assertThat(userService.getUserByUsername("ghost")).isEmpty();
    }

    @Test
    void getUserById_shouldReturnMatchingUser() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        Optional<UserEntity> found = userService.getUserById(existing.id());

        // then.
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(existing.id());
        assertThat(found.get().email()).isEqualTo("a@example.com");
        assertThat(found.get().provider()).isEqualTo(Provider.LOCAL);
    }

    @Test
    void getUserById_shouldReturnEmptyWhenNotFound() {
        // when. / then.
        assertThat(userService.getUserById("non-existent-id")).isEmpty();
    }

    @Test
    void updateLastLoginAt_shouldSetLastLoginAtToNow() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);
        Instant before = Instant.now();

        // when.
        userService.updateLastLoginAt("alice");

        // then.
        UserEntity updated = userRepository.findByUsername("alice").orElseThrow();
        assertThat(updated.lastLoginAt()).isNotNull();
    }

    @Test
    void updateUserPatch_shouldUpdateAllProvidedFields() {
        userService.create("oldName", "old@example.com", "oldPass", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("oldName").orElseThrow();

        // when.
        userService.updateUserPatch(existing.id(), "newName", "new@example.com", "newPass", Set.of("ADMIN", "EDITOR"));

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.username()).isEqualTo("newName");
        assertThat(updated.email()).isEqualTo("new@example.com");
        assertThat(updated.roles().values()).containsExactlyInAnyOrder("ADMIN", "EDITOR");
        assertThat(passwordEncoder.matches("newPass", updated.password())).isTrue();
    }

    @Test
    void updateUserPatch_shouldClearEmailAndKeepPasswordWhenArgsAreNull() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateUserPatch(
                existing.id(), "renamed", null, null, existing.roles().values());

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.username()).isEqualTo("renamed");
        assertThat(updated.email()).isNull();
        assertThat(updated.roles().values()).isEqualTo(existing.roles().values());
        assertThat(updated.password()).isEqualTo(existing.password());
    }

    @Test
    void updateUserPatch_shouldHashNewPassword() {
        userService.create("alice", "a@example.com", "oldPass", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateUserPatch(
                existing.id(),
                existing.username(),
                null,
                "newPass",
                existing.roles().values());

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.password()).isNotEqualTo("newPass"); // Hash password
        assertThat(passwordEncoder.matches("newPass", updated.password())).isTrue();
    }

    @Test
    void updateUserPatch_shouldThrowWhenUserIdNotFound() {
        // when. / then.
        assertThatThrownBy(
                        () -> userService.updateUserPatch("non-existent-id", "anyName", null, null, Set.of("VIEWER")))
                .isInstanceOf(UserWithIdNotFoundException.class);
    }

    @Test
    void updateUserPatch_shouldThrowWhenRoleIsNotAllowed() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(existing.id(), "alice", null, null, Set.of("SUPER_ADMIN")))
                .isInstanceOf(UserRoleNotFoundException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.roles().values()).containsExactly("VIEWER");
    }

    @Test
    void updateUserPatch_shouldThrowWhenUsernameIsBlank() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() ->
                        userService.updateUserPatch(existing.id(), "   ", "alice@example.com", "p", Set.of("VIEWER")))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.username()).isEqualTo("alice");
    }

    @Test
    void updateUserPatch_shouldThrowWhenEmailIsBlank() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(existing.id(), "alice", "   ", "p", Set.of("VIEWER")))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.email()).isEqualTo("a@example.com");
    }

    @Test
    void updateUserPatch_shouldThrowWhenPasswordIsBlank() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        existing.id(), "alice", "alice@example.com", "   ", Set.of("VIEWER")))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.password()).isEqualTo(existing.password());
    }

    @Test
    void updateUserPatch_shouldThrowWhenRolesContainBlank() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        existing.id(), "alice", "alice@example.com", "p", Set.of("VIEWER", "   ")))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.roles().values()).containsExactly("VIEWER");
    }

    @Test
    void updateUserPatch_shouldReplaceRolesCompletely() {
        userService.create("alice", "a@example.com", "p", "VIEWER", Provider.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateUserPatch(existing.id(), existing.username(), null, null, Set.of("ADMIN"));

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.roles().values()).containsExactly("ADMIN");
    }
}
