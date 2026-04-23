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

import com.axelixlabs.axelix.master.api.external.request.user.UserCreateRequest;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.exception.auth.RoleNotPresentException;
import com.axelixlabs.axelix.master.exception.auth.UserIdNotFoundException;
import com.axelixlabs.axelix.master.service.auth.Provider;

/**
 * Service that manages the lifecycle of users persisted by the Users Management API.
 *
 * <p>Provides operations to create, read, update, and delete {@link UserEntity} records,
 * as well as to track login activity. Passwords supplied to this service are plain-text and
 * are hashed by the implementation before persistence. Usernames and emails are expected to
 * be unique; attempts to violate those constraints surface as
 * {@link org.springframework.dao.DataIntegrityViolationException}.
 *
 * @author Sergey Cherkasov
 */
public interface UserManaged {

    /**
     * Creates a new managed user from a Users Management API request.
     *
     * @param user     Payload containing the username, email, plain-text password, and role name.
     * @param provider Origin of the account (e.g. {@link Provider#LOCAL} for login/password users).
     * @throws RoleNotPresentException if the provided role does not exist in the service.
     */
    void create(UserCreateRequest user, Provider provider);

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
     * Atomically applies a partial update to the user with the given id. Every non-null argument
     * is applied; {@code null} arguments leave the corresponding field untouched. All changes are
     * performed within a single transaction, so a failure in any individual field update rolls
     * back the entire patch.
     *
     * @param id       Unique identifier of the user to update.
     * @param username New login username, or {@code null} to leave it unchanged. Must be unique when not {@code null}.
     * @param email    New email address, or {@code null} to leave it unchanged. Must be unique when not {@code null}.
     * @param roles    New set of role names, or {@code null} to leave them unchanged. Replaces any roles previously assigned.
     * @param password New plain-text password (hashed server-side), or {@code null} to leave it unchanged.
     * @throws UserIdNotFoundException if no user with the given id exists.
     * @throws RoleNotPresentException if the provided role does not exist in the service.
     */
    void updateUserPatch(
            String id,
            @Nullable String username,
            @Nullable String email,
            @Nullable Set<String> roles,
            @Nullable String password)
            throws RoleNotPresentException, UserIdNotFoundException;
}
