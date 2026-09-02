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
package com.axelixlabs.axelix.common.auth.core;

import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * An abstraction of the user, who tries to interact with Axelix in any shape or form.
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public interface User {

    /**
     * Stable, immutable identifier of the user. Unlike {@link #getUsername()}, this value
     * never changes for a given user and is therefore the safe key to correlate a user
     * across logins.
     */
    String getId();

    /**
     * Human-facing username of the given user. Username is by it nature mutable and thus <strong>must not</strong>
     * be used as a stable identity - use {@link #getId()} for that purpose.
     */
    String getUsername();

    /**
     * Password of the given user. It might be null.
     */
    @Nullable
    String getPassword();

    /**
     * Set of {@link Role roles} that are assigned to this User.
     */
    Set<Role> getRoles();
}
