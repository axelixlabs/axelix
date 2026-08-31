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

import java.util.Collection;

/**
 * Exception thrown when a managed user cannot be found.
 *
 * @author Mikhail Polivakha
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String id) {
        super("User with id '" + id + "' was not found");
    }

    public UserNotFoundException(Collection<String> ids) {
        super("Some of the users with ids %s were not found".formatted(ids));
    }
}
