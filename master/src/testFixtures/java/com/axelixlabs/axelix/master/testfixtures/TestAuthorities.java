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
package com.axelixlabs.axelix.master.testfixtures;

import com.axelixlabs.axelix.master.domain.auth.Authority;

/**
 * Authority codenames used by tests of built-in permissions.
 *
 * @deprecated This is the wrong way to initialize the authorities in tests. The source of
 *             truth for authorities is supposed to be the database, and only it. As a result,
 *             using this test authorities is simply wrong, we should instead insert test records
 *             in the database and then clean them up afterwards.
 *
 * @author Mikhail Polivakha
 */
@Deprecated(forRemoval = true)
public enum TestAuthorities implements Authority {
    ENV_VALUES_READ("environment.values:read"),
    CONFIG_PROPS_VALUES_READ("config-properties.values:read"),
    SCHEDULED_TASKS_MODIFY("scheduled-tasks:write"),
    CACHES_CLEAR("caches:clear"),
    CACHES_TOGGLE("caches:toggle"),
    GARBAGE_COLLECTOR("gc:manage"),
    USERS_VIEW("users:read"),
    USERS_MANAGEMENT("users:manage");

    private final String codename;

    TestAuthorities(String codename) {
        this.codename = codename;
    }

    @Override
    public String getName() {
        return codename;
    }
}
