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
 * The technical user, i.e. it represents some part of the Axelix system,
 * e.g. managed service (the one that includes starter) or the master.
 * <p>
 * The technical user is not supposed to have any roles. In other words, if the
 * token we receive contains the technical user and is signed with the appropriate
 * signing key we expect, we can trust that this server is the one it claims to be.
 * <p>
 * The key is that the "roles" of this component in the system are already known.
 * For example, we know that the starter is supposed to call the heartbeat, so we don't
 * need its roles for that. Also, if the request comes from the master side, the master
 * has already performed all necessary RBAC activities, and the starter can simply accept
 * the token from the master without verifying whether the end user actually has those authorities.
 *
 * @author Mikhail Polivakha
 */
public enum TechUser {
    MASTER("AXELIX.MASTER"),
    MANAGED_SERVICE("AXELX.MANAGED_SERVICE");

    private final String id;

    TechUser(String id) {
        this.id = id;
    }

    /**
     * @return stable identifier of the part of the given part of the system
     */
    public String getId() {
        return id;
    }
}
