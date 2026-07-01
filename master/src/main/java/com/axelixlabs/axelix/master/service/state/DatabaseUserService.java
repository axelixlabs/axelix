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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.exception.auth.UserDuplicateValueException;
import com.axelixlabs.axelix.master.exception.auth.UserInvalidValueException;
import com.axelixlabs.axelix.master.exception.auth.UserRoleNotFoundException;
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

    public DatabaseUserService(
            UserRepository userRepository,
            JdbcAggregateTemplate jdbcAggregateTemplate,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createLocal(String username, @Nullable String email, String password, String role)
            throws UserRoleNotFoundException, UserInvalidValueException, UserDuplicateValueException {

        UserEntity userEntity = new UserEntity(
                UUID.randomUUID().toString(),
                requireNonBlankTrimmed(username),
                email == null ? null : requireNonBlankTrimmed(email),
                passwordEncoder.encode(requireNonBlankTrimmed(password)),
                new UserEntity.Roles(Set.of(validateAndNormalizeRole(role))),
                UserOrigin.LOCAL,
                null);

        insertNewUser(userEntity);
    }

    @Override
    public void createFromOidc(String username, @Nullable String email, String role)
            throws UserRoleNotFoundException, UserInvalidValueException, UserDuplicateValueException {

        UserEntity userEntity = new UserEntity(
                UUID.randomUUID().toString(),
                requireNonBlankTrimmed(username),
                email == null ? null : requireNonBlankTrimmed(email),
                null,
                new UserEntity.Roles(Set.of(validateAndNormalizeRole(role))),
                UserOrigin.OIDC,
                Instant.now()); // the assumption is that the user is created during the initial login

        insertNewUser(userEntity);
    }

    private void insertNewUser(UserEntity userEntity) throws UserDuplicateValueException {
        try {
            jdbcAggregateTemplate.insert(userEntity);
        } catch (DuplicateKeyException | UncategorizedSQLException e) {
            // TODO: develop a proper approach for handling SQLite errors so that only actual
            //  unique constraint violations are mapped to UserDuplicateValueException, instead
            //  of blindly treating every untranslated UncategorizedSQLException as a duplicate
            throw new UserDuplicateValueException(e);
        }
    }

    @Override
    public void deleteById(String id) {
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
    public void updateLastLoginAt(String username) {
        userRepository.updateLastLoginAt(username, Instant.now());
    }

    @Override
    public void updateUserPatch(
            String id,
            String username,
            @Nullable String email,
            @Nullable String password,
            Set<String> roles,
            @Nullable Instant lastLoginAt)
            throws UserRoleNotFoundException, UserInvalidValueException, UserDuplicateValueException {

        if (roles.isEmpty()) {
            throw new UserInvalidValueException(null);
        }

        Set<String> validRoles =
                roles.stream().map(this::validateAndNormalizeRole).collect(Collectors.toSet());

        try {
            userRepository.updateUserPatch(
                    id,
                    requireNonBlankTrimmed(username),
                    email == null ? null : requireNonBlankTrimmed(email),
                    password == null ? null : passwordEncoder.encode(requireNonBlankTrimmed(password)),
                    new UserEntity.Roles(validRoles),
                    lastLoginAt);
        } catch (DuplicateKeyException | UncategorizedSQLException e) {
            // TODO: develop a proper approach for handling SQLite errors so that only actual
            //  unique constraint violations are mapped to UserDuplicateValueException, instead
            //  of blindly treating every untranslated UncategorizedSQLException as a duplicate
            throw new UserDuplicateValueException(e);
        }
    }

    private String requireNonBlankTrimmed(String value) throws UserInvalidValueException {
        if (!value.isBlank()) {
            return value.trim();
        }

        throw new UserInvalidValueException(value);
    }

    private String validateAndNormalizeRole(@Nullable String role)
            throws UserInvalidValueException, UserRoleNotFoundException {
        if (role == null || role.isBlank()) {
            throw new UserInvalidValueException(role);
        }

        String normalized = role.trim().toUpperCase();
        if (!DefaultRole.ALL_ROLE_NAMES.contains(normalized)) {
            throw new UserRoleNotFoundException(role);
        }
        return normalized;
    }
}
