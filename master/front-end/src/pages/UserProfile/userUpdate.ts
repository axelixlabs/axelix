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
import type { ERoles, IEditUserRequestData, IUser } from "@/models";

/**
 * Builds an update that keeps every field the caller does not override. The update replaces names, so omitting them
 * would clear the stored values.
 */
export const userUpdateFrom = (
    user: IUser,
    overrides: {
        username?: string;
        firstName?: string | null;
        lastName?: string | null;
        email?: string | null;
        password?: string | null;
        roles?: ERoles[];
    } = {},
): IEditUserRequestData => {
    return {
        id: user.id,
        username: overrides.username ?? user.username,
        firstName: overrides.firstName !== undefined ? overrides.firstName : user.firstName,
        lastName: overrides.lastName !== undefined ? overrides.lastName : user.lastName,
        email: overrides.email !== undefined ? overrides.email : user.email,
        password: overrides.password ?? null,
        roles: overrides.roles ?? user.roles,
    };
};
