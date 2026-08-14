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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.SuperAdminConfigurationProperties;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.domain.UserStatus;
import com.axelixlabs.axelix.master.exception.auth.EmailAlreadyExistsException;
import com.axelixlabs.axelix.master.exception.auth.UserInvalidValueException;
import com.axelixlabs.axelix.master.exception.auth.UserNotFoundException;
import com.axelixlabs.axelix.master.exception.auth.UserRoleNotFoundException;
import com.axelixlabs.axelix.master.exception.auth.UsernameAlreadyExistsException;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

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
@DatabaseMatrixTest
class DatabaseUserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SuperAdminConfigurationProperties superAdminConfiguration;

    @BeforeEach
    @AfterEach
    void setup() {
        userRepository.findAll().forEach(user -> userService.deleteById(user.id()));
    }

    @Test
    void createLocal_shouldPersistUser() {
        // when.
        userService.createLocal(
                "alice",
                " Alice ",
                " Smith ",
                "alice@example.com",
                " Software Engineer ",
                " Platform ",
                "plainPass",
                "ADMIN");

        // then.
        List<UserEntity> users = userRepository.findAll();
        assertThat(users).hasSize(1);

        UserEntity saved = users.get(0);
        assertThat(saved.id()).isNotBlank();
        assertThat(saved.username()).isEqualTo("alice");
        assertThat(saved.firstName()).isEqualTo("Alice");
        assertThat(saved.lastName()).isEqualTo("Smith");
        assertThat(saved.email()).isEqualTo("alice@example.com");
        assertThat(saved.jobTitle()).isEqualTo("Software Engineer");
        assertThat(saved.organizationalUnit()).isEqualTo("Platform");
        assertThat(saved.roles().values()).containsExactly("ADMIN");
        assertThat(saved.userOrigin()).isEqualTo(UserOrigin.LOCAL);
        assertThat(saved.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.password()).isNotEqualTo("plainPass"); // Hash password
        assertThat(passwordEncoder.matches("plainPass", saved.password())).isTrue();
        assertThat(saved.lastLoginAt()).isNull();
    }

    @Test
    void createFroOidc_shouldAllowNullEmail() {
        // when.
        userService.createFromOidc("bob", null, null, null, null, null, "VIEWER");

        // then.
        UserEntity saved = userRepository.findByUsername("bob").orElseThrow();
        assertThat(saved.firstName()).isNull();
        assertThat(saved.lastName()).isNull();
        assertThat(saved.email()).isNull();
        assertThat(saved.jobTitle()).isNull();
        assertThat(saved.organizationalUnit()).isNull();
        assertThat(saved.password()).isNull();
        assertThat(saved.userOrigin()).isEqualTo(UserOrigin.OIDC);
        assertThat(saved.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void createFromOidc_shouldThrowWhenUsernameIsReservedForSuperAdmin() {
        // when.
        assertThatThrownBy(() -> userService.createFromOidc(
                        superAdminConfiguration.getUsername(),
                        null,
                        null,
                        "impostor@example.com",
                        null,
                        null,
                        "VIEWER"))
                // then.
                .isInstanceOf(UsernameAlreadyExistsException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenRoleIsNotAllowed() {
        // when.
        assertThatThrownBy(() -> userService.createLocal(
                        "alice", null, null, "alice@example.com", null, null, "p", "SUPER_ADMIN"))
                // then.
                .isInstanceOf(UserRoleNotFoundException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test // TODO: This test should be revisited since in enterprise we're going to be able to supply many roles
    void createLocal_shouldThrowWhenRoleDoesNotExist() {
        // when.
        assertThatThrownBy(() -> userService.createLocal(
                        "alice", null, null, "alice@example.com", null, null, "p", "NOT_A_ROLE"))
                // then.
                .isInstanceOf(UserRoleNotFoundException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenUsernameIsBlank() {
        // when.
        assertThatThrownBy(() ->
                        userService.createLocal("   ", null, null, "alice@example.com", null, null, "p", "VIEWER"))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenEmailIsBlank() {
        // when.
        assertThatThrownBy(() -> userService.createLocal("alice", null, null, "   ", null, null, "p", "VIEWER"))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenPasswordIsBlank() {
        // when.
        assertThatThrownBy(() ->
                        userService.createLocal("alice", null, null, "alice@example.com", null, null, "   ", "VIEWER"))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenRoleIsBlank() {
        // when.
        assertThatThrownBy(
                        () -> userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "   "))
                // then.
                .isInstanceOf(UserInvalidValueException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenUsernameAlreadyExists() {
        // given.
        userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "VIEWER");

        // when.
        assertThatThrownBy(() ->
                        userService.createLocal("alice", null, null, "other@example.com", null, null, "p", "VIEWER"))
                // then.
                .isInstanceOf(UsernameAlreadyExistsException.class);
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void createLocal_shouldThrowWhenUsernameIsReservedForSuperAdmin() {
        // when.
        assertThatThrownBy(() -> userService.createLocal(
                        superAdminConfiguration.getUsername(),
                        null,
                        null,
                        "impostor@example.com",
                        null,
                        null,
                        "p",
                        "VIEWER"))
                // then.
                .isInstanceOf(UsernameAlreadyExistsException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void createLocal_shouldThrowWhenEmailAlreadyExists() {
        // given.
        userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "VIEWER");

        // when.
        assertThatThrownBy(() ->
                        userService.createLocal("bob", null, null, "alice@example.com", null, null, "p", "VIEWER"))
                // then.
                .isInstanceOf(EmailAlreadyExistsException.class);
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void deleteAllById_shouldRemoveUser() {
        userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "VIEWER");
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
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
        userService.createFromOidc("bob", null, null, "b@example.com", null, null, "ADMIN");

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
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");

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
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
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
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");

        // when.
        userService.updateLastLoginAt("alice");

        // then.
        UserEntity updated = userRepository.findByUsername("alice").orElseThrow();
        assertThat(updated.lastLoginAt()).isNotNull();
    }

    @Test
    void updateStatus_shouldUpdateOnlyStatus() {
        // given.
        userService.createLocal("alice", "Alice", "Smith", "a@example.com", null, null, "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateStatus(existing.id(), UserStatus.SUSPENDED);

        // then.
        UserEntity saved = userRepository.findById(existing.id()).orElseThrow();
        assertThat(saved.status()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(saved).usingRecursiveComparison().ignoringFields("status").isEqualTo(existing);
    }

    @Test
    void updateStatus_shouldBeIdempotent() {
        // given.
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateStatus(existing.id(), UserStatus.ACTIVE);

        // then.
        assertThat(userRepository.findById(existing.id()).orElseThrow().status())
                .isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void updateStatus_shouldReportUnknownUser() {
        // when.
        assertThatThrownBy(() -> userService.updateStatus("non-existent-id", UserStatus.SUSPENDED))
                // then.
                .isInstanceOf(UserNotFoundException.class);
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void updateUserPatch_shouldUpdateAllProvidedFields() {
        userService.createLocal("oldName", "First", "Last", "old@example.com", null, null, "oldPass", "VIEWER");
        UserEntity existing = userRepository.findByUsername("oldName").orElseThrow();

        // when.
        userService.updateUserPatch(
                existing.id(),
                "newName",
                "First",
                "Last",
                "new@example.com",
                "Engineering Manager",
                "Engineering",
                "newPass",
                Set.of("ADMIN", "EDITOR"),
                null);

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.username()).isEqualTo("newName");
        assertThat(updated.firstName()).isEqualTo("First");
        assertThat(updated.lastName()).isEqualTo("Last");
        assertThat(updated.email()).isEqualTo("new@example.com");
        assertThat(updated.jobTitle()).isEqualTo("Engineering Manager");
        assertThat(updated.organizationalUnit()).isEqualTo("Engineering");
        assertThat(updated.roles().values()).containsExactlyInAnyOrder("ADMIN", "EDITOR");
        assertThat(passwordEncoder.matches("newPass", updated.password())).isTrue();
    }

    @Test
    void updateUserPatch_shouldUpdateAllProvidedFields_PasswordIsNotProvided() {
        // given.
        String oldPassword = "oldPass";
        userService.createLocal("oldName", null, null, "old@example.com", null, null, oldPassword, "VIEWER");
        UserEntity existing = userRepository.findByUsername("oldName").orElseThrow();

        // when.
        userService.updateUserPatch(
                existing.id(),
                "newName",
                null,
                null,
                "new@example.com",
                null,
                null,
                null,
                Set.of("ADMIN", "EDITOR"),
                null);

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.username()).isEqualTo("newName");
        assertThat(updated.email()).isEqualTo("new@example.com");
        assertThat(updated.roles().values()).containsExactlyInAnyOrder("ADMIN", "EDITOR");
        assertThat(passwordEncoder.matches(oldPassword, updated.password())).isTrue();
    }

    @Test
    void updateUserPatch_shouldHashNewPassword() {
        userService.createLocal("alice", null, null, "a@example.com", null, null, "oldPass", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateUserPatch(
                existing.id(),
                existing.username(),
                null,
                null,
                null,
                null,
                null,
                "newPass",
                existing.roles().values(),
                null);

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.password()).isNotEqualTo("newPass"); // Hash password
        assertThat(passwordEncoder.matches("newPass", updated.password())).isTrue();
    }

    @Test
    void updateUserPatch_shouldThrowWhenRoleIsNotAllowed() {
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        existing.id(), "alice", null, null, null, null, null, null, Set.of("SUPER_ADMIN"), null))
                .isInstanceOf(UserRoleNotFoundException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.roles().values()).containsExactly("VIEWER");
    }

    @Test
    void updateUserPatch_shouldThrowWhenUsernameIsBlank() {
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        existing.id(), "   ", null, null, "alice@example.com", null, null, "p", Set.of("VIEWER"), null))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.username()).isEqualTo("alice");
    }

    @Test
    void updateUserPatch_shouldThrowWhenEmailIsBlank() {
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        existing.id(), "alice", null, null, "   ", null, null, "p", Set.of("VIEWER"), null))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.email()).isEqualTo("a@example.com");
    }

    @Test
    void updateUserPatch_shouldThrowWhenPasswordIsBlank() {
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        existing.id(),
                        "alice",
                        null,
                        null,
                        "alice@example.com",
                        null,
                        null,
                        "   ",
                        Set.of("VIEWER"),
                        null))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.password()).isEqualTo(existing.password());
    }

    @Test
    void updateUserPatch_shouldThrowWhenRolesAreEmpty() {
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        existing.id(), "alice", null, null, "alice@example.com", null, null, "p", Set.of(), null))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.roles().values()).containsExactly("VIEWER");
    }

    @Test
    void updateUserPatch_shouldThrowWhenRolesContainBlank() {
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when. / then.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        existing.id(),
                        "alice",
                        null,
                        null,
                        "alice@example.com",
                        null,
                        null,
                        "p",
                        Set.of("VIEWER", "   "),
                        null))
                .isInstanceOf(UserInvalidValueException.class);

        UserEntity untouched = userRepository.findById(existing.id()).orElseThrow();
        assertThat(untouched.roles().values()).containsExactly("VIEWER");
    }

    @Test
    void updateUserPatch_shouldThrowWhenUsernameAlreadyExists() {
        // given.
        userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "VIEWER");
        userService.createLocal("bob", null, null, "bob@example.com", null, null, "p", "VIEWER");
        UserEntity bob = userRepository.findByUsername("bob").orElseThrow();

        // when.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        bob.id(), "alice", null, null, "bob@example.com", null, null, null, Set.of("VIEWER"), null))
                // then.
                .isInstanceOf(UsernameAlreadyExistsException.class);

        UserEntity untouched = userRepository.findById(bob.id()).orElseThrow();
        assertThat(untouched.username()).isEqualTo("bob");
    }

    @Test
    void updateUserPatch_shouldThrowWhenUsernameIsReservedForSuperAdmin() {
        // given.
        userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "VIEWER");
        UserEntity alice = userRepository.findByUsername("alice").orElseThrow();

        // when.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        alice.id(),
                        superAdminConfiguration.getUsername(),
                        null,
                        null,
                        "alice@example.com",
                        null,
                        null,
                        null,
                        Set.of("VIEWER"),
                        null))
                // then.
                .isInstanceOf(UsernameAlreadyExistsException.class);

        UserEntity untouched = userRepository.findById(alice.id()).orElseThrow();
        assertThat(untouched.username()).isEqualTo("alice");
    }

    @Test
    void updateUserPatch_shouldThrowWhenEmailAlreadyExists() {
        // given.
        userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "VIEWER");
        userService.createLocal("bob", null, null, "bob@example.com", null, null, "p", "VIEWER");
        UserEntity bob = userRepository.findByUsername("bob").orElseThrow();

        // when.
        assertThatThrownBy(() -> userService.updateUserPatch(
                        bob.id(), "bob", null, null, "alice@example.com", null, null, null, Set.of("VIEWER"), null))
                // then.
                .isInstanceOf(EmailAlreadyExistsException.class);

        UserEntity untouched = userRepository.findById(bob.id()).orElseThrow();
        assertThat(untouched.email()).isEqualTo("bob@example.com");
    }

    @Test
    void updateUserPatch_shouldAllowUserToKeepItsOwnUsernameAndEmail() {
        // given.
        userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "VIEWER");
        UserEntity alice = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateUserPatch(
                alice.id(), "alice", null, null, "alice@example.com", null, null, null, Set.of("ADMIN"), null);

        // then.
        UserEntity updated = userRepository.findById(alice.id()).orElseThrow();
        assertThat(updated.username()).isEqualTo("alice");
        assertThat(updated.email()).isEqualTo("alice@example.com");
        assertThat(updated.roles().values()).containsExactly("ADMIN");
    }

    @Test
    void updateUserPatch_shouldReplaceRolesCompletely() {
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateUserPatch(
                existing.id(),
                existing.username(),
                null,
                null,
                existing.email(),
                null,
                null,
                "newPass",
                Set.of("ADMIN"),
                null);

        // then.
        UserEntity updated = userRepository.findById(existing.id()).orElseThrow();
        assertThat(updated.roles().values()).containsExactly("ADMIN");
        assertThat(passwordEncoder.matches("newPass", updated.password())).isTrue();
    }

    @Test
    void createLocal_shouldGrantRolesThroughUsersRolesTable() {
        // when.
        userService.createLocal("alice", null, null, "alice@example.com", null, null, "plainPass", "ADMIN");

        // then.
        UserEntity saved = userRepository.findByUsername("alice").orElseThrow();
        assertThat(userService.findRoleNamesByUserId(saved.id())).containsExactly("ADMIN");
    }

    @Test
    void createFromOidc_shouldGrantRolesThroughUsersRolesTable() {
        // when.
        userService.createFromOidc("bob", null, null, "bob@example.com", null, null, "VIEWER");

        // then.
        UserEntity saved = userRepository.findByUsername("bob").orElseThrow();
        assertThat(userService.findRoleNamesByUserId(saved.id())).containsExactly("VIEWER");
    }

    @Test
    void updateUserPatch_shouldReplaceRolesInUsersRolesTableRatherThanAddToThem() {
        // given.
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "VIEWER");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.updateUserPatch(
                existing.id(),
                existing.username(),
                null,
                null,
                existing.email(),
                null,
                null,
                null,
                Set.of("ADMIN", "EDITOR"),
                null);

        // then.
        assertThat(userService.findRoleNamesByUserId(existing.id())).containsExactlyInAnyOrder("ADMIN", "EDITOR");
    }

    @Test
    void deleteById_shouldRevokeRoles() {
        // given.
        userService.createLocal("alice", null, null, "a@example.com", null, null, "p", "ADMIN");
        UserEntity existing = userRepository.findByUsername("alice").orElseThrow();

        // when.
        userService.deleteById(existing.id());

        // then.
        assertThat(userService.findRoleNamesByUserId(existing.id())).isEmpty();
    }

    @Test
    void findAllRoleNamesByUserId_shouldReturnAssignmentsOfEveryUser() {
        // given.
        userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "ADMIN");
        userService.createLocal("bob", null, null, "bob@example.com", null, null, "p", "VIEWER");

        String aliceId = userRepository.findByUsername("alice").orElseThrow().id();
        String bobId = userRepository.findByUsername("bob").orElseThrow().id();

        // when.
        Map<String, Set<String>> roleNamesByUserId = userService.findAllRoleNamesByUserId();

        // then.
        assertThat(roleNamesByUserId).containsOnlyKeys(aliceId, bobId);
        assertThat(roleNamesByUserId.get(aliceId)).containsExactly("ADMIN");
        assertThat(roleNamesByUserId.get(bobId)).containsExactly("VIEWER");
    }
}
