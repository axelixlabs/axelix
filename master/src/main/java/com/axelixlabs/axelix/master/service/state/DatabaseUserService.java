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

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.exception.auth.UserInvalidValueException;
import com.axelixlabs.axelix.master.exception.auth.UserRoleNotFoundException;
import com.axelixlabs.axelix.master.exception.auth.UserWithIdNotFoundException;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.Provider;

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
    public void create(
            String username, @Nullable String email, @Nullable String password, String role, Provider provider)
            throws UserRoleNotFoundException, UserInvalidValueException {

        UserEntity userEntity = new UserEntity(
                UUID.randomUUID().toString(),
                requireNonBlankTrimmed(username),
                email == null ? null : requireNonBlankTrimmed(email),
                password == null ? null : passwordEncoder.encode(requireNonBlankTrimmed(password)),
                new UserEntity.Roles(Set.of(validateAndNormalizeRole(role))),
                provider,
                null);

        jdbcAggregateTemplate.insert(userEntity);
    }

    @Override
    public void delete(String id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<UserEntity> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<UserEntity> getUserById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public void updateLastLoginAt(String username) {
        userRepository
                .findByUsername(username)
                .ifPresent(user -> jdbcAggregateTemplate.update(user.withLastLoginAt(Instant.now())));
    }

    @Override
    public void updateUserPatch(
            String id, String username, @Nullable String email, @Nullable String password, Set<String> roles)
            throws UserRoleNotFoundException, UserWithIdNotFoundException, UserInvalidValueException {

        Set<String> validRoles =
                roles.stream().map(this::validateAndNormalizeRole).collect(Collectors.toSet());

        UserEntity user = userRepository.findById(id).orElseThrow(() -> new UserWithIdNotFoundException(id));

        String newPassword =
                password != null ? passwordEncoder.encode(requireNonBlankTrimmed(password)) : user.password();
        jdbcAggregateTemplate.update(new UserEntity(
                id,
                requireNonBlankTrimmed(username),
                email == null ? null : requireNonBlankTrimmed(email),
                newPassword,
                new UserEntity.Roles(validRoles),
                user.provider(),
                user.lastLoginAt()));
    }

    private String requireNonBlankTrimmed(@Nullable String value) throws UserInvalidValueException {
        if (value != null && !value.isBlank()) {
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
