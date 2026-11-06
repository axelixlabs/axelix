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

/**
 * Authorities that well-known .
 *
 * @see Authority
 *
 * @deprecated since 1.3, the only source of truth for the authorities will be the state in the database.
 *             This enum is no longer for the starters that are 1.3+. It is retained solely for the purpose
 *             of ensuring the backward compatibility window for old version of starters that expect authorities
 *             to exist in the token.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@Deprecated(forRemoval = true, since = "1.3")
public class OssAuthority {

    public static final Authority ENV_VALUES_READ_LEGACY = new DefaultAuthority("ENV_VALUES_READ");

    //    /**
    //     * Grants the right to view values in Environment.
    //     */
    //    ENV_VALUES_READ("environment.values:read"),
    //
    //    /**
    //     * Grants the right to view values in Configuration Properties.
    //     */
    //    CONFIG_PROPS_VALUES_READ("config-properties.values:read"),
    //
    //    /**
    //     * Grants the right to modify scheduled tasks.
    //     * <p>Allows modifying individual tasks, forcing their execution, and enabling/disabling them at runtime.</p>
    //     */
    //    SCHEDULED_TASKS_MODIFY("scheduled-tasks:write"),
    //
    //    /**
    //     * Grants the right to clear caches.
    //     * <p>Allows clearing individual caches, cache managers, and all app cache at runtime.</p>
    //     */
    //    CACHES_CLEAR("caches:clear"),
    //
    //    /**
    //     * Grants the right to enable/disable caches.
    //     * <p>Allows enabling/disabling individual caches and cache managers at runtime.</p>
    //     */
    //    CACHES_TOGGLE("caches:toggle"),
    //
    //    /**
    //     * Grants permission to perform actions for garbage collector log monitoring.
    //     * <p>Allows triggering, enabling, and disabling garbage collector log monitoring at runtime.</p>
    //     */
    //    GARBAGE_COLLECTOR("gc:manage"),
    //
    //    /**
    //     * Grants the right to view managed users.
    //     */
    //    USERS_VIEW("users:read"),
    //
    //    /**
    //     * Grants the right to manage users.
    //     * <p>Allows creating, updating, and deleting managed users at runtime.</p>
    //     */
    //    USERS_MANAGEMENT("users:manage");
    //
    //    private final String codename;
    //
    //    OssAuthority(String codename) {
    //        this.codename = codename;
    //    }
    //
    //    public String getCodename() {
    //        return codename;
    //    }
    //
    //    public static Set<Authority> asSet() {
    //        return Arrays.stream(values()).collect(Collectors.toSet());
    //    }
    //
    //    @Override
    //    public String getName() {
    //        return name();
    //    }
}
