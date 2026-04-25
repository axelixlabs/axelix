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

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.exception.auth.UserInvalidValueException;
import com.axelixlabs.axelix.master.exception.auth.UserRoleNotFoundException;
import com.axelixlabs.axelix.master.exception.auth.UserWithIdNotFoundException;
import com.axelixlabs.axelix.master.service.auth.Provider;

/**
 * Service that manages the lifecycle of users persisted by the Users Management API.
 *
 * <p>Passwords supplied to this service are plain-text and are hashed by the implementation before persistence.
 * Usernames and emails are expected to be unique.
 *
 * @author Sergey Cherkasov
 */
public interface UserService {

    /**
     * Creates a new managed user from a Users Management API request.
     *
     * @param username Login username of the new user. Must be unique and not blank or {@code null}.
     * @param email    Email address of the new user, or {@code null} if not provided. Must be unique and not blank.
     * @param password Plain-text password (hashed server-side before persistence), or {@code null} for accounts that
     *                 do not use password auth. Must not be blank.
     * @param role     Role name to assign to the new user. Must not be blank or {@code null}.
     * @param provider Origin of the account (e.g. {@link Provider#OIDC} {@link Provider#LOCAL}).
     * @throws UserRoleNotFoundException if the provided role does not exist in the service.
     * @throws UserInvalidValueException if any of the provided string fields is blank.
     */
    void create(String username, @Nullable String email, @Nullable String password, String role, Provider provider);

    /**
     * Deletes the user with the given identifier. No-op if the user does not exist.
     *
     * @param id Unique identifier of the user to delete.
     */
    void delete(String id);

    /**
     * Returns all managed users.
     *
     * @return All persisted {@link UserEntity} records, or an empty list if none exist.
     */
    List<UserEntity> getAll();

    /**
     * Looks up a user by login username.
     *
     * @param username Login username to search for.
     * @return The matching user, or {@link Optional#empty()} if no user with that username exists.
     */
    Optional<UserEntity> getUserByUsername(String username);

    /**
     * Looks up a user by unique identifier.
     *
     * @param id Unique identifier of the user.
     * @return The matching user, or {@link Optional#empty()} if no user with that id exists.
     */
    Optional<UserEntity> getUserById(String id);

    /**
     * Sets the user's {@code lastLoginAt} timestamp to {@link Instant#now()}.
     * No-op if no user with the given username exists.
     *
     * @param username Login username of the user who just authenticated.
     */
    void updateLastLoginAt(String username);

    /**
     * Applies a partial update to the user with the given id within a single transaction.
     *
     * <p>Field semantics:
     * <ul>
     *   <li>{@code username} and {@code roles} are required and must not be {@code null}.</li>
     *   <li>{@code email} is nullable: passing {@code null} <b>clears</b> the existing email value.</li>
     *   <li>{@code password} is nullable: passing {@code null} <b>preserves</b> the previously stored password.</li>
     * </ul>
     *
     * @param id       Unique identifier of the user to update.
     * @param username New login username. Must be unique and not blank.
     * @param email    New email address, or {@code null} to clear the stored email. Must be unique and not blank.
     * @param password New plain-text password (hashed server-side), or {@code null} to keep the existing password.
     *                 Must not be blank when.
     * @param roles    New set of role names. Replaces any roles previously assigned. Each role must not be blank or {@code null}.
     * @throws UserWithIdNotFoundException if no user with the given id exists.
     * @throws UserRoleNotFoundException if any of the provided role names does not exist in the service.
     * @throws UserInvalidValueException if any of the provided string fields is blank.
     */
    void updateUserPatch(
            String id, String username, @Nullable String email, @Nullable String password, Set<String> roles)
            throws UserRoleNotFoundException, UserWithIdNotFoundException;
}
