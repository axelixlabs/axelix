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
package com.axelixlabs.axelix.master.service.state;

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
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.exception.auth.UserInvalidValueException;
import com.axelixlabs.axelix.master.exception.auth.UserRoleNotFoundException;
import com.axelixlabs.axelix.master.repository.UserRepository;

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
        userService.create("alice", "alice@example.com", "plainPass", "ADMIN", UserOrigin.LOCAL);

        // then.
        List<UserEntity> users = userRepository.findAll();
        assertThat(users).hasSize(1);

        UserEntity saved = users.get(0);
        assertThat(saved.id()).isNotBlank();
        assertThat(saved.username()).isEqualTo("alice");
        assertThat(saved.email()).isEqualTo("alice@example.com");
        assertThat(saved.roles().values()).containsExactly("ADMIN");
        assertThat(saved.userOrigin()).isEqualTo(UserOrigin.LOCAL);
        assertThat(saved.password()).isNotEqualTo("plainPass"); // Hash password
        assertThat(passwordEncoder.matches("plainPass", saved.password())).isTrue();
        assertThat(saved.lastLoginAt()).isNull();
    }

    @Test
    void create_shouldAllowNullEmail() {
        // when.
        userService.create("bob", null, "plainPass", "VIEWER", UserOrigin.OIDC);

        // then.
        UserEntity saved = userRepository.findByUsername("bob").orElseThrow();
        assertThat(saved.email()).isNull();
        assertThat(passwordEncoder.matches("plainPass", saved.password())).isTrue();
        assertThat(saved.userOrigin()).isEqualTo(UserOrigin.OIDC);
    }

    @Test
    void create_shouldThrowWhenRoleIsNotAllowed() {
        // when.
        assertThatThrownBy(() -> userService.create("alice", "alice@example.com", "p", "SUPER_ADMIN", UserOrigin.LOCAL))
                // then.
                .isInstanceOf(UserRoleNotFoundException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void create_shouldThrowWhenRoleDoesNotExist() {
        // when.
        assertThatThrownBy(() -> userService.create("alice", "alice@example.com", "p", "NOT_A_ROLE", UserOrigin.LOCAL))
                // then.
                .isInstanceOf(UserRoleNotFoundException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void create_shouldThrowWhenUsernameIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.create("   ", "alice@example.com", "p", "VIEWER", UserOrigin.LOCAL))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void create_shouldThrowWhenEmailIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.create("alice", "   ", "p", "VIEWER", UserOrigin.LOCAL))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void create_shouldThrowWhenPasswordIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.create("alice", "alice@example.com", "   ", "VIEWER", UserOrigin.LOCAL))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void create_shouldThrowWhenRoleIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.create("alice", "alice@example.com", "p", "   ", UserOrigin.LOCAL))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void deleteAllById_shouldRemoveUser() {
        userService.create("alice", "alice@example.com", "p", "VIEWER", UserOrigin.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.deleteById(existing.id());

        // then.
        assertThat(userRepository.findById(existing.id())).isEmpty();
    }

    @Test
    void deleteAllById_shouldBeNoOpWhenUserDoesNotExist() {
        // when.
        assertThatCode(() -> userService.deleteById("non-existent-id")).doesNotThrowAnyException();
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userService.create("alice", "a@example.com", "p", "VIEWER", UserOrigin.LOCAL);
        userService.create("bob", "b@example.com", "p", "ADMIN", UserOrigin.OIDC);

        // when.
        List<UserEntity> all = userService.findAll();

        // then.
        assertThat(all).extracting(UserEntity::username).containsExactlyInAnyOrder("alice", "bob");
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoUsers() {
        // when. / then.
        assertThat(userService.findAll()).isEmpty();
    }

    @Test
    void findUserByUsername_shouldReturnMatchingUser() {
        userService.create("alice", "a@example.com", "p", "VIEWER", UserOrigin.LOCAL);

        // when.
        Optional<UserEntity> found = userService.findUserByUsername("alice");

        // then.
        assertThat(found).isPresent();
        assertThat(found.get().username()).isEqualTo("alice");
        assertThat(found.get().email()).isEqualTo("a@example.com");
        assertThat(found.get().userOrigin()).isEqualTo(UserOrigin.LOCAL);
    }

    @Test
    void findUserByUsername_shouldReturnEmptyWhenNotFound() {
        // when. / then.
        assertThat(userService.findUserByUsername("ghost")).isEmpty();
    }

    @Test
    void findUserById_shouldReturnMatchingUser() {
        userService.create("alice", "a@example.com", "p", "VIEWER", UserOrigin.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        Optional<UserEntity> found = userService.findUserById(existing.id());

        // then.
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(existing.id());
        assertThat(found.get().email()).isEqualTo("a@example.com");
        assertThat(found.get().userOrigin()).isEqualTo(UserOrigin.LOCAL);
    }

    @Test
    void findUserById_shouldReturnEmptyWhenNotFound() {
        // when. / then.
        assertThat(userService.findUserById("non-existent-id")).isEmpty();
    }

    @Test
    void updateLastLoginAt_shouldSetLastLoginAtToNow() {
        userService.create("alice", "a@example.com", "p", "VIEWER", UserOrigin.LOCAL);

        // when.
        userService.updateLastLoginAt("alice");

        // then.
        UserEntity updated = userRepository.findByUsername("alice").orElseThrow();
        assertThat(updated.lastLoginAt()).isNotNull();
    }

    @Test
    void updateUserPatch_shouldUpdateAllProvidedFields() {
        userService.create("oldName", "old@example.com", "oldPass", "VIEWER", UserOrigin.LOCAL);
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
    void updateUserPatch_shouldUpdateAllProvidedFields_PasswordIsNotProvided() {
        // given.
        String oldPassword = "oldPass";
        userService.create("oldName", "old@example.com", oldPassword, "VIEWER", UserOrigin.LOCAL);
        UserEntity existing = userRepository.findByUsername("oldName").orElseThrow();

        // when.
        userService.updateUserPatch(existing.id(), "newName", "new@example.com", null, Set.of("ADMIN", "EDITOR"));

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.username()).isEqualTo("newName");
        assertThat(updated.email()).isEqualTo("new@example.com");
        assertThat(updated.roles().values()).containsExactlyInAnyOrder("ADMIN", "EDITOR");
        assertThat(passwordEncoder.matches(oldPassword, updated.password())).isTrue();
    }

    @Test
    void updateUserPatch_shouldHashNewPassword() {
        userService.create("alice", "a@example.com", "oldPass", "VIEWER", UserOrigin.LOCAL);
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
    void updateUserPatch_shouldThrowWhenRoleIsNotAllowed() {
        userService.create("alice", "a@example.com", "p", "VIEWER", UserOrigin.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(existing.id(), "alice", null, null, Set.of("SUPER_ADMIN")))
                .isInstanceOf(UserRoleNotFoundException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.roles().values()).containsExactly("VIEWER");
    }

    @Test
    void updateUserPatch_shouldThrowWhenUsernameIsBlank() {
        userService.create("alice", "a@example.com", "p", "VIEWER", UserOrigin.LOCAL);
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
        userService.create("alice", "a@example.com", "p", "VIEWER", UserOrigin.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(existing.id(), "alice", "   ", "p", Set.of("VIEWER")))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.email()).isEqualTo("a@example.com");
    }

    @Test
    void updateUserPatch_shouldThrowWhenPasswordIsBlank() {
        userService.create("alice", "a@example.com", "p", "VIEWER", UserOrigin.LOCAL);
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
        userService.create("alice", "a@example.com", "p", "VIEWER", UserOrigin.LOCAL);
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
        userService.create("alice", "a@example.com", "p", "VIEWER", UserOrigin.LOCAL);
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateUserPatch(existing.id(), existing.username(), existing.email(), "newPass", Set.of("ADMIN"));

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.roles().values()).containsExactly("ADMIN");
        assertThat(passwordEncoder.matches("newPass", updated.password())).isTrue();
    }
}
