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
package com.axelixlabs.axelix.master.domain;

/**
 * Origin of a managed user account.
 *
 * @author Sergey Cherkasov
 */
public enum UserOrigin {
    /**
     * Account originated from an external OIDC/OAuth2 identity origin.
     */
    OIDC("OAUTH2/OIDC"),

    /**
     * Account created and managed locally within Axelix (e.g. via the Users Management API).
     */
    LOCAL("LOCAL");

    /**
     * Human-readable label of this origin, suitable for display in the UI.
     */
    private final String displayName;

    UserOrigin(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
