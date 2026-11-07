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
import { ERoles } from "@/models";

// Authorities seeded for the built-in roles. ADMIN includes every EDITOR authority.
const RUNTIME_AUTHORITIES = ["SCHEDULED_TASKS_MODIFY", "CACHES_CLEAR", "CACHES_TOGGLE", "GARBAGE_COLLECTOR"] as const;

const SENSITIVE_AUTHORITIES = ["ENV_VALUES_READ", "CONFIG_PROPS_VALUES_READ"] as const;

export const PROFILE_AUTHORITIES = [...RUNTIME_AUTHORITIES, ...SENSITIVE_AUTHORITIES] as const;

export type TProfileAuthority = (typeof PROFILE_AUTHORITIES)[number];

export const PERMISSION_GROUPS = [
    { id: "runtime", authorities: RUNTIME_AUTHORITIES },
    { id: "sensitive", authorities: SENSITIVE_AUTHORITIES },
] as const;

const ROLE_RANK: Record<ERoles, number> = {
    [ERoles.VIEWER]: 0,
    [ERoles.EDITOR]: 1,
    [ERoles.ADMIN]: 2,
};

const GRANTS: Record<ERoles, readonly TProfileAuthority[]> = {
    [ERoles.VIEWER]: [],
    [ERoles.EDITOR]: RUNTIME_AUTHORITIES,
    [ERoles.ADMIN]: PROFILE_AUTHORITIES,
};

// Least privileged first, matching the change-roles dialog
export const PROFILE_ROLES = [ERoles.VIEWER, ERoles.EDITOR, ERoles.ADMIN];

export const compareRoles = (left: ERoles, right: ERoles): number => ROLE_RANK[left] - ROLE_RANK[right];

/**
 * Authorities granted by the given roles. A permission can come from more than one role, so this does not attribute it
 * to a single role.
 */
export const effectiveAuthorities = (roles: readonly ERoles[]): Set<TProfileAuthority> => {
    const granted = new Set<TProfileAuthority>();

    roles.forEach((role) => {
        GRANTS[role].forEach((authority) => granted.add(authority));
    });

    return granted;
};
