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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.master.api.external.request.user.UserCreateRequest;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.exception.auth.RoleNotPresentException;
import com.axelixlabs.axelix.master.exception.auth.UserIdNotFoundException;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.Provider;

/**
 * JDBC-based implementation of {@link UserManaged} that persists users in a relational database.
 *
 * @author Sergey Cherkasov
 */
@Service
@NullMarked
@Transactional
public class DatabaseUserManaged implements UserManaged {

    private final UserRepository userRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final PasswordEncoder passwordEncoder;

    public DatabaseUserManaged(
            UserRepository userRepository,
            JdbcAggregateTemplate jdbcAggregateTemplate,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void create(UserCreateRequest user, Provider provider) throws RoleNotPresentException {
        validateRole(user.role());
        UserEntity userEntity = new UserEntity(
                UUID.randomUUID().toString(),
                user.username(),
                user.email(),
                user.password() == null ? null : passwordEncoder.encode(user.password()),
                new UserEntity.Roles(Set.of(user.role())),
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
            String id,
            @Nullable String username,
            @Nullable String email,
            @Nullable Set<String> roles,
            @Nullable String password)
            throws RoleNotPresentException, UserIdNotFoundException {

        UserEntity user = userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id));

        if (username != null) {
            user = user.withUsername(username);
        }
        if (email != null) {
            user = user.withEmail(email);
        }
        if (roles != null) {
            roles.forEach(DatabaseUserManaged::validateRole);
            user = user.withRoles(new UserEntity.Roles(roles));
        }
        if (password != null) {
            user = user.withPassword(passwordEncoder.encode(password));
        }

        jdbcAggregateTemplate.update(user);
    }

    private static void validateRole(@Nullable String role) {
        if (role == null || !DefaultRole.ALL_ROLE_NAMES.contains(role)) {
            throw new RoleNotPresentException(role);
        }
    }
}
