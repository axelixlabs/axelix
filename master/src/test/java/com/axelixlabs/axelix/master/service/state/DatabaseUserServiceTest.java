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
import com.axelixlabs.axelix.master.exception.auth.UserNotDeletedException;
import com.axelixlabs.axelix.master.exception.auth.UserRoleNotFoundException;
import com.axelixlabs.axelix.master.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Base class for integration tests of {@link DatabaseUserService}.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
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
    void createLocal_shouldPersistUser() {
        // when.
        userService.createLocal("alice", "alice@example.com", "plainPass", "ADMIN");

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
    void createFroOidc_shouldAllowNullEmail() {
        // when.
        userService.createFromOidc("bob", null, "VIEWER");

        // then.
        UserEntity saved = userRepository.findByUsername("bob").orElseThrow();
        assertThat(saved.email()).isNull();
        assertThat(saved.password()).isNull();
        assertThat(saved.userOrigin()).isEqualTo(UserOrigin.OIDC);
    }

    @Test
    void createLocal_shouldThrowWhenRoleIsNotAllowed() {
        // when.
        assertThatThrownBy(() -> userService.createLocal("alice", "alice@example.com", "p", "SUPER_ADMIN"))
                // then.
                .isInstanceOf(UserRoleNotFoundException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test // TODO: This test should be revisited since in enterprise we're going to be able to supply many roles
    void createLocal_shouldThrowWhenRoleDoesNotExist() {
        // when.
        assertThatThrownBy(() -> userService.createLocal("alice", "alice@example.com", "p", "NOT_A_ROLE"))
                // then.
                .isInstanceOf(UserRoleNotFoundException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenUsernameIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.createLocal("   ", "alice@example.com", "p", "VIEWER"))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenEmailIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.createLocal("alice", "   ", "p", "VIEWER"))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenPasswordIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.createLocal("alice", "alice@example.com", "   ", "VIEWER"))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenRoleIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.createLocal("alice", "alice@example.com", "p", "   "))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void deleteByIdToLocalUser_shouldRemoveLocalUser() {
        userService.createLocal("alice", "alice@example.com", "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        assertThatCode(() -> userService.deleteByIdToLocalUser(existing.id()))
                // then.
                .doesNotThrowAnyException();
        assertThat(userRepository.findById(existing.id())).isEmpty();
    }

    @Test
    void deleteByIdToLocalUser_shouldNotRemoveOidcUser() {
        userService.createFromOidc("bob", "bob@example.com", "VIEWER");
        UserEntity existing = userRepository.findByUsername("bob").orElseThrow();

        // when.
        assertThatThrownBy(() -> userService.deleteByIdToLocalUser(existing.id()))
                // then.
                .isInstanceOf(UserNotDeletedException.class);

        Optional<UserEntity> stillPresent = userRepository.findById(existing.id());
        assertThat(stillPresent).isPresent();
        assertThat(stillPresent.get().userOrigin()).isEqualTo(UserOrigin.OIDC);
    }

    @Test
    void deleteByIdToLocalUser_nonExistentId_shouldThrow() {
        userService.createLocal("alice", "alice@example.com", "p", "VIEWER");
        userService.createFromOidc("bob", "bob@example.com", "VIEWER");
        UserEntity localUser = userRepository.findByUsername("alice").orElseThrow();
        UserEntity oidcUser = userRepository.findByUsername("bob").orElseThrow();

        // when.
        assertThatThrownBy(() -> userService.deleteByIdToLocalUser("non-existent-id"))
                // then.
                .isInstanceOf(UserNotDeletedException.class);

        assertThat(userRepository.findById(localUser.id())).isPresent();
        assertThat(userRepository.findById(oidcUser.id())).isPresent();
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");
        userService.createFromOidc("bob", "b@example.com", "ADMIN");

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
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");

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
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");
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
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");

        // when.
        userService.updateLastLoginAt("alice");

        // then.
        UserEntity updated = userRepository.findByUsername("alice").orElseThrow();
        assertThat(updated.lastLoginAt()).isNotNull();
    }

    @Test
    void updateUserPatch_shouldUpdateAllProvidedFields() {
        userService.createLocal("oldName", "old@example.com", "oldPass", "VIEWER");
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
        userService.createLocal("oldName", "old@example.com", oldPassword, "VIEWER");
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
        userService.createLocal("alice", "a@example.com", "oldPass", "VIEWER");
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
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(existing.id(), "alice", null, null, Set.of("SUPER_ADMIN")))
                .isInstanceOf(UserRoleNotFoundException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.roles().values()).containsExactly("VIEWER");
    }

    @Test
    void updateUserPatch_shouldThrowWhenUsernameIsBlank() {
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");
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
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(existing.id(), "alice", "   ", "p", Set.of("VIEWER")))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.email()).isEqualTo("a@example.com");
    }

    @Test
    void updateUserPatch_shouldThrowWhenPasswordIsBlank() {
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        existing.id(), "alice", "alice@example.com", "   ", Set.of("VIEWER")))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.password()).isEqualTo(existing.password());
    }

    @Test
    void updateUserPatch_shouldThrowWhenRolesAreEmpty() {
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(
                        () -> userService.updateUserPatch(existing.id(), "alice", "alice@example.com", "p", Set.of()))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.roles().values()).containsExactly("VIEWER");
    }

    @Test
    void updateUserPatch_shouldThrowWhenRolesContainBlank() {
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");
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
        userService.createLocal("alice", "a@example.com", "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateUserPatch(existing.id(), existing.username(), existing.email(), "newPass", Set.of("ADMIN"));

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.roles().values()).containsExactly("ADMIN");
        assertThat(passwordEncoder.matches("newPass", updated.password())).isTrue();
    }
}
