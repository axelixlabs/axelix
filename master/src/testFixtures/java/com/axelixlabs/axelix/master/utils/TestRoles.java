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
package com.axelixlabs.axelix.master.utils;

import java.util.Set;

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;

/**
 * The built-in Axelix roles, as tests need to see them.
 *
 * @author Sergey Cherkasov
 */
public final class TestRoles {

    public static final Role VIEWER = new DefaultRole("VIEWER", Set.of());

    public static final Role EDITOR = new DefaultRole(
            "EDITOR",
            Set.of(
                    DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                    DefaultAuthority.CACHES_CLEAR,
                    DefaultAuthority.CACHES_TOGGLE,
                    DefaultAuthority.GARBAGE_COLLECTOR));

    public static final Role ADMIN = new DefaultRole(
            "ADMIN",
            Set.of(
                    DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                    DefaultAuthority.CACHES_CLEAR,
                    DefaultAuthority.CACHES_TOGGLE,
                    DefaultAuthority.GARBAGE_COLLECTOR,
                    DefaultAuthority.ENV_VALUES_READ,
                    DefaultAuthority.CONFIG_PROPS_VALUES_READ));

    private TestRoles() {}
}
