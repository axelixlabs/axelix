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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.domain.UserStatus;
import com.axelixlabs.axelix.master.exception.auth.EmailAlreadyExistsException;
import com.axelixlabs.axelix.master.exception.auth.UserInvalidValueException;
import com.axelixlabs.axelix.master.exception.auth.UserNotFoundException;
import com.axelixlabs.axelix.master.exception.auth.UserRoleNotFoundException;
import com.axelixlabs.axelix.master.exception.auth.UsernameAlreadyExistsException;

/**
 * Service that manages the lifecycle of users persisted by the Users Management API.
 *
 * <p>Passwords supplied to this service are plain-text and <strong>MUST</strong> be hashed by the
 * implementation before persistence. Usernames and emails are expected to be unique.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@NullMarked
public interface UserService {

    /**
     * Creates a new {@link UserOrigin#LOCAL} user.
     *
     * @param username Login username of the new user. Must be unique.
     * @param firstName First name of the new user, or {@code null} if not provided.
     * @param lastName  Last name of the new user, or {@code null} if not provided.
     * @param email    Email address of the new user, or {@code null} if not provided. If supplied, must be unique.
     * @param jobTitle Job title of the new user, or {@code null} if not provided.
     * @param organizationalUnit Organizational unit of the new user, or {@code null} if not provided.
     * @param password Plain-text password (must be hashed server-side before persistence).
     * @param role     Role name to assign to the new user. Must not be blank or {@code null}.
     *
     * @throws UserRoleNotFoundException if the provided role does not exist in the service.
     * @throws UserInvalidValueException if any of the provided string fields is blank.
     * @throws UsernameAlreadyExistsException if a user with the given username already exists.
     * @throws EmailAlreadyExistsException if a user with the given email already exists.
     */
    void createLocal(
            String username,
            @Nullable String firstName,
            @Nullable String lastName,
            @Nullable String email,
            @Nullable String jobTitle,
            @Nullable String organizationalUnit,
            String password,
            String role);

    /**
     * Creates a new {@link UserOrigin#OIDC} user.
     *
     * @param username Login username of the new user. Must be unique.
     * @param firstName First name of the new user, or {@code null} if not provided.
     * @param lastName  Last name of the new user, or {@code null} if not provided.
     * @param email    Email address of the new user, or {@code null} if not provided. If supplied, must be unique.
     * @param jobTitle Job title of the new user, or {@code null} if not provided.
     * @param organizationalUnit Organizational unit of the new user, or {@code null} if not provided.
     * @param role     Role name to assign to the new user. Must not be blank or {@code null}.
     *
     * @throws UserRoleNotFoundException if the provided role does not exist in the service.
     * @throws UserInvalidValueException if any of the provided string fields is blank.
     * @throws UsernameAlreadyExistsException if the given username is reserved for the super-admin.
     */
    void createFromOidc(
            String username,
            @Nullable String firstName,
            @Nullable String lastName,
            @Nullable String email,
            @Nullable String jobTitle,
            @Nullable String organizationalUnit,
            String role);

    /**
     * Deletes the user with the given identifier. No-op if the user does not exist.
     *
     * @param id Unique identifier of the user to delete.
     */
    void deleteById(String id);

    /**
     * Returns all managed users.
     *
     * @return All persisted {@link UserEntity} records, or an empty list if none exist.
     */
    List<UserEntity> findAll();

    /**
     * Looks up a user by login username.
     *
     * @param username Login username to search for.
     * @return The matching user, or {@link Optional#empty()} if no user with that username exists.
     */
    Optional<UserEntity> findUserByUsername(String username);

    /**
     * Looks up a user by unique identifier.
     *
     * @param id Unique identifier of the user.
     * @return The matching user, or {@link Optional#empty()} if no user with that id exists.
     */
    Optional<UserEntity> findUserById(String id);

    /**
     * Returns the names of the roles granted to the given user.
     *
     * @param userId Unique identifier of the user.
     * @return Names of the granted roles, empty if the user has none or does not exist.
     */
    Set<String> findRoleNamesByUserId(String userId);

    /**
     * Returns the role names of every user, keyed by user id. Intended for listing users without issuing a query
     * per user.
     *
     * @return Role names per user id. Users without roles are absent from the map.
     */
    Map<String, Set<String>> findAllRoleNamesByUserId();

    /**
     * Sets the user's {@code lastLoginAt} timestamp to {@link Instant#now()}.
     * No-op if no user with the given username exists.
     *
     * @param username Login username of the user who just authenticated.
     */
    void updateLastLoginAt(String username);

    /**
     * Changes the status of a persisted user without modifying other user data.
     *
     * @param id Unique identifier of the user.
     * @param status New status of the user.
     * @throws UserNotFoundException if no user with the given id exists.
     */
    void updateStatus(String id, UserStatus status);

    /**
     * Applies a partial update to the user with the given id within a single transaction.
     *
     * <p>Field semantics:
     * <ul>
     *   <li>{@code username} and {@code roles} are required and must not be {@code null}.</li>
     *   <li>{@code firstName}, {@code lastName}, {@code email}, {@code jobTitle}, and
     *       {@code organizationalUnit} are nullable: passing {@code null} clears them.</li>
     *   <li>{@code password} is nullable: passing {@code null} <b>preserves</b> the previously stored password.</li>
     *   <li>{@code lastLoginAt} is nullable: passing {@code null} preserves the existing timestamp.</li>
     * </ul>
     *
     * @param id       Unique identifier of the user to update.
     * @param username New login username. Must be unique and not blank.
     * @param firstName New first name, or {@code null} to clear the stored first name.
     * @param lastName New last name, or {@code null} to clear the stored last name.
     * @param email    New email address, or {@code null} to clear the stored email. Must be unique and not blank.
     * @param jobTitle New job title, or {@code null} to clear the stored job title.
     * @param organizationalUnit New organizational unit, or {@code null} to clear the stored organizational unit.
     * @param password New plain-text password (hashed server-side), or {@code null} to keep the existing password.
     *                 Must not be blank when.
     * @param roles    New set of role names. Replaces any roles previously assigned. Each role must not be blank or {@code null}.
     * @param lastLoginAt New last-login timestamp, or {@code null} to preserve the stored timestamp.
     * @throws UserRoleNotFoundException if any of the provided role names does not exist in the service.
     * @throws UserInvalidValueException if any of the provided string fields is blank.
     * @throws UsernameAlreadyExistsException if another user with the given username already exists.
     * @throws EmailAlreadyExistsException if another user with the given email already exists.
     */
    void updateUserPatch(
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
            throws UserRoleNotFoundException, UserInvalidValueException;
}
