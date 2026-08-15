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
package com.axelixlabs.axelix.master.service.auth.provider;

import org.jspecify.annotations.Nullable;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserStatus;
import com.axelixlabs.axelix.master.exception.auth.UserSuspendedException;
import com.axelixlabs.axelix.master.service.state.RoleService;
import com.axelixlabs.axelix.master.service.state.UserService;

/**
 * {@link UserAuthenticator} that authenticates a given user against the users stored in the database.
 *
 * @author Sergey Cherkasov
 */
@Transactional
public class DatabaseUserAuthenticator implements UserAuthenticator {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public DatabaseUserAuthenticator(
            UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public @Nullable User authenticate(String username, String password) {

        // TODO:
        //  Maybe we can load user with his roles already here with one query?
        //  It is going to be a bit inconvenient, since we would have to write
        //  the mapping of a non-flat result set by ourselves (we're using SDJ).
        UserEntity user = userService.findUserByUsername(username).orElse(null);

        if (user != null && user.password() != null && passwordEncoder.matches(password, user.password())) {
            if (user.status() == UserStatus.SUSPENDED) {
                throw new UserSuspendedException();
            }

            userService.updateLastLoginAt(user.username());
            return new DefaultUser(user.username(), user.password(), roleService.findRolesOfUser(user.id()));
        }

        return null;
    }
}
