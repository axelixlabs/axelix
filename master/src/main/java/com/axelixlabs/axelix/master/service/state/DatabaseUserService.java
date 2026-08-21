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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * JDBC-based implementation of {@link UserService} that persists users in a relational database.
 *
 * @author Sergey Cherkasov
 */
@Service
@NullMarked
@Transactional
public class DatabaseUserService implements UserService {

    private final UserRepository userRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminConfigurationProperties superAdminConfiguration;

    public DatabaseUserService(
            UserRepository userRepository,
            JdbcAggregateTemplate jdbcAggregateTemplate,
            PasswordEncoder passwordEncoder,
            SuperAdminConfigurationProperties superAdminConfiguration) {
        this.userRepository = userRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.passwordEncoder = passwordEncoder;
        this.superAdminConfiguration = superAdminConfiguration;
    }

    @Override
    public void createLocal(
            String username,
            @Nullable String firstName,
            @Nullable String lastName,
            @Nullable String email,
            @Nullable String jobTitle,
            @Nullable String organizationalUnit,
            String password,
            String role)
            throws UserRoleNotFoundException, UserInvalidValueException {

        UserEntity userEntity = new UserEntity(
                UUID.randomUUID().toString(),
                requireNonBlankTrimmed(username),
                normalizeOptional(firstName),
                normalizeOptional(lastName),
                normalizeOptional(email),
                normalizeOptional(jobTitle),
                normalizeOptional(organizationalUnit),
                passwordEncoder.encode(requireNonBlankTrimmed(password)),
                null,
                UserOrigin.LOCAL,
                UserStatus.ACTIVE,
                null);

        if (isUsernameReservedForSuperAdmin(userEntity.username())
                || userRepository.findByUsername(userEntity.username()).isPresent()) {
            throw new UsernameAlreadyExistsException(userEntity.username());
        }

        String userEmail = userEntity.email();
        if (userEmail != null && userRepository.findByEmail(userEmail).isPresent()) {
            throw new EmailAlreadyExistsException(userEmail);
        }

        jdbcAggregateTemplate.insert(userEntity);
        grantRoles(userEntity.id(), Collections.singleton(role));
    }

    @Override
    public void createFromOidc(
            String username,
            @Nullable String firstName,
            @Nullable String lastName,
            @Nullable String email,
            @Nullable String jobTitle,
            @Nullable String organizationalUnit,
            String role) {

        UserEntity userEntity = new UserEntity(
                UUID.randomUUID().toString(),
                requireNonBlankTrimmed(username),
                normalizeOptional(firstName),
                normalizeOptional(lastName),
                normalizeOptional(email),
                normalizeOptional(jobTitle),
                normalizeOptional(organizationalUnit),
                null,
                null,
                UserOrigin.OIDC,
                UserStatus.ACTIVE,
                Instant.now()); // the assumption is that the user is created during the initial login

        if (isUsernameReservedForSuperAdmin(userEntity.username())) {
            throw new UsernameAlreadyExistsException(userEntity.username());
        }

        jdbcAggregateTemplate.insert(userEntity);
        grantRoles(userEntity.id(), Set.of(role));
    }

    @Override
    public void deleteById(String id) {
        userRepository.deleteUserRolesMappings(id);
        userRepository.deleteById(id);
    }

    @Override
    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<UserEntity> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<UserEntity> findUserById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Set<String> findRoleNamesByUserId(String userId) {
        return Set.copyOf(userRepository.findRoleNamesByUserId(userId));
    }

    @Override
    public Map<String, Set<String>> findAllRoleNamesByUserId() {
        return userRepository.findAllUserRoleNames().stream()
                .collect(Collectors.groupingBy(
                        UserRepository.UserRoleName::userId,
                        Collectors.mapping(UserRepository.UserRoleName::roleName, Collectors.toSet())));
    }

    @Override
    public void updateLastLoginAt(String username) {
        userRepository.updateLastLoginAt(username, Instant.now());
    }

    @Override
    public void updateStatus(String id, UserStatus status) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.updateStatus(id, status);
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void updateUserPatch(
            String id,
            String username,
            @Nullable String firstName,
            @Nullable String lastName,
            @Nullable String email,
            @Nullable String jobTitle,
            @Nullable String organizationalUnit,
            @Nullable String password,
            Set<String> roles,
            @Nullable Instant lastLoginAt)
            throws UserRoleNotFoundException, UserInvalidValueException {

        if (roles.isEmpty()) {
            throw new UserInvalidValueException(null);
        }

        String normalizedUsername = requireNonBlankTrimmed(username);
        String normalizedFirstName = normalizeOptional(firstName);
        String normalizedLastName = normalizeOptional(lastName);
        String normalizedEmail = normalizeOptional(email);
        String normalizedJobTitle = normalizeOptional(jobTitle);
        String normalizedOrganizationalUnit = normalizeOptional(organizationalUnit);

        if (isUsernameReservedForSuperAdmin(normalizedUsername)
                || userWithSuchUsernameAlreadyExists(id, normalizedUsername)) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }

        if (normalizedEmail != null) {
            if (userWithSuchEmailAlreadyExists(id, normalizedEmail)) {
                throw new EmailAlreadyExistsException(normalizedEmail);
            }
        }

        userRepository.updateUserPatch(
                id,
                normalizedUsername,
                normalizedFirstName,
                normalizedLastName,
                normalizedEmail,
                normalizedJobTitle,
                normalizedOrganizationalUnit,
                password == null ? null : passwordEncoder.encode(requireNonBlankTrimmed(password)),
                lastLoginAt);

        userRepository.deleteUserRolesMappings(id);
        grantRoles(id, roles);
    }

    // TODO: Right now this is acceptable (i.e. a non-bulk INSERT) but in the future that may need optimization
    private void grantRoles(String userId, Set<String> roleNames) {
        roleNames.forEach(roleName -> {
            String normalizedRole = validateAndNormalizeRole(roleName);
            int rowsAffected = userRepository.attachRole(userId, normalizedRole);

            if (rowsAffected != 1) {
                throw new UserRoleNotFoundException(normalizedRole);
            }
        });
    }

    private String validateAndNormalizeRole(@Nullable String role) throws UserInvalidValueException {
        if (role == null || role.isBlank()) {
            throw new UserInvalidValueException(role);
        }

        return role.trim().toUpperCase(Locale.ROOT);
    }

    private boolean userWithSuchEmailAlreadyExists(String id, String normalizedEmail) {
        return userRepository
                .findByEmail(normalizedEmail)
                .filter(it -> !Objects.equals(it.id(), id))
                .isPresent();
    }

    private boolean userWithSuchUsernameAlreadyExists(String id, String normalizedUsername) {
        return userRepository
                .findByUsername(normalizedUsername)
                .filter(it -> !Objects.equals(it.id(), id))
                .isPresent();
    }

    private boolean isUsernameReservedForSuperAdmin(String username) {
        return superAdminConfiguration.getUsername().equals(username);
    }

    private String requireNonBlankTrimmed(String value) throws UserInvalidValueException {
        if (!value.isBlank()) {
            return value.trim();
        }

        throw new UserInvalidValueException(value);
    }

    @Nullable
    private String normalizeOptional(@Nullable String value) throws UserInvalidValueException {
        return value == null ? null : requireNonBlankTrimmed(value);
    }
}
