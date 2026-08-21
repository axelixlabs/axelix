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
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserStatus;

/**
 * Repository for {@link UserEntity} aggregate.
 *
 * @author Sergey Cherkasov
 */
public interface UserRepository extends ListCrudRepository<UserEntity, String> {

    Optional<UserEntity> findByUsername(@Param("username") String username);

    Optional<UserEntity> findByEmail(@Param("email") String email);

    @Query("SELECT r.name FROM users_roles ur JOIN roles r ON r.id = ur.role_id WHERE ur.user_id = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") String userId);

    @Query("SELECT ur.user_id AS user_id, r.name AS role_name FROM users_roles ur JOIN roles r ON r.id = ur.role_id")
    List<UserRoleName> findAllUserRoleNames();

    /**
     * @return the amount of rows affected
     */
    @Modifying
    @Query("INSERT INTO users_roles (user_id, role_id) SELECT :userId, r.id FROM roles r WHERE r.name = :roleName")
    int attachRole(@Param("userId") String userId, @Param("roleName") String roleName);

    @Modifying
    @Query("DELETE FROM users_roles WHERE user_id = :userId")
    void deleteUserRolesMappings(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE users SET last_login_at = :lastLoginAt WHERE username = :username")
    void updateLastLoginAt(@Param("username") String username, @Param("lastLoginAt") Instant lastLoginAt);

    @Modifying
    @Query("UPDATE users SET status = :status WHERE id = :id")
    void updateStatus(@Param("id") String id, @Param("status") UserStatus status);

    @Modifying
    @Query("""
        UPDATE users
        SET
                username = :username,
                first_name = :firstName,
                last_name = :lastName,
                email = :email,
                job_title = :jobTitle,
                organizational_unit = :organizationalUnit,
                password = COALESCE(:password, password),
                last_login_at = COALESCE(:lastLoginAt, last_login_at)
        WHERE id = :id
        """)
    void updateUserPatch(
            @Param("id") String id,
            @Param("username") String username,
            @Param("firstName") @Nullable String firstName,
            @Param("lastName") @Nullable String lastName,
            @Param("email") @Nullable String email,
            @Param("jobTitle") @Nullable String jobTitle,
            @Param("organizationalUnit") @Nullable String organizationalUnit,
            @Param("password") @Nullable String password,
            @Param("lastLoginAt") @Nullable Instant lastLoginAt);

    /**
     * Single role assignment, used to read the assignments of many users at once without a query per user.
     *
     * @param userId   Identifier of the user the role is granted to.
     * @param roleName Name of the granted role.
     *
     * @author Sergey Cherkasov
     */
    record UserRoleName(String userId, String roleName) {}
}
