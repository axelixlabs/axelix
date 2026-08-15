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
package com.axelixlabs.axelix.common.testfixtures;

import java.util.Set;

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;

// TODO:
//  Having that test fixture is fine for now, but this is (to me) clearly a code smell.
//  The problem is that in the future, when the authorities are going to be added, they
//  are gonna be added in the DB as the migration. And we will have to manually sync the
//  authorities of the ROLE outlined in the new migration with the authorities in here.
//  In plain English - we potentially repeat ourselves. Now, we may name such roles in some
//  artificial fashion, so that they are not really VIEWER or EDITOR, but in this case, we
//  are gonna need to revisit their usages. I guess that some testing code explicitly relies
//  on the role being VIEWER/EDITOR/ADMIN (although, it must not! This is a code smell)

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
