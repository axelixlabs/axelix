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
package com.axelixlabs.axelix.master.repository;

import java.time.Instant;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.axelixlabs.axelix.master.domain.UserEntity;

/**
 * Repository for {@link UserEntity} aggregate.
 *
 * @author Sergey Cherkasov
 */
public interface UserRepository extends ListCrudRepository<UserEntity, String> {

    Optional<UserEntity> findByUsername(@Param("username") String username);

    @Modifying
    @Query("UPDATE users SET last_login_at = :lastLoginAt WHERE username = :username")
    void updateLastLoginAt(@Param("username") String username, @Param("lastLoginAt") Instant lastLoginAt);

    @Modifying
    @Query("UPDATE users SET username = :username, email = :email, password = :password, roles = :roles WHERE id = :id")
    void updateUserPatch(
            @Param("id") String id,
            @Param("username") String username,
            @Param("email") @Nullable String email,
            @Param("password") @Nullable String password,
            @Param("roles") UserEntity.Roles roles);

    @Modifying
    @Query("UPDATE users SET username = :username, email = :email, roles = :roles WHERE id = :id")
    void updateUserPatchWithoutPassword(
            @Param("id") String id,
            @Param("username") String username,
            @Param("email") @Nullable String email,
            @Param("roles") UserEntity.Roles roles);
}
