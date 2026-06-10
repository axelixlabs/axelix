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
package com.axelixlabs.axelix.master.exception.auth;

import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.service.state.UserService;

/**
 * Thrown when a user deletion did not affect any record, i.e. no user exists with the given id or the
 * user is not a {@link UserOrigin#LOCAL} user (only local users may be deleted).
 *
 * @see UserService
 * @author Sergey Cherkasov
 */
public class UserNotDeletedException extends RuntimeException {

    public UserNotDeletedException(String id) {
        super("User with id '%s' was not deleted: it does not exist or is not a LOCAL user".formatted(id));
    }
}
