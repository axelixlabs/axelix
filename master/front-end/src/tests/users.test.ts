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
import { describe, expect, it } from "vitest";

import { countUsersWithRole } from "@/helpers";
import { ERoles, type IUser } from "@/models";

const userWithRoles = (id: string, roles: ERoles[]): IUser =>
    ({
        id,
        roles,
    }) as IUser;

describe("countUsersWithRole", () => {
    it("counts a user once for each role they hold", () => {
        // given.
        const users = [
            userWithRoles("1", [ERoles.ADMIN]),
            userWithRoles("2", [ERoles.ADMIN, ERoles.EDITOR]),
            userWithRoles("3", [ERoles.VIEWER]),
        ];

        // then.
        expect(countUsersWithRole(users, ERoles.ADMIN)).toBe(2);
        expect(countUsersWithRole(users, ERoles.EDITOR)).toBe(1);
        expect(countUsersWithRole(users, ERoles.VIEWER)).toBe(1);
    });

    it("returns zero when nobody holds the role", () => {
        expect(countUsersWithRole([userWithRoles("1", [ERoles.VIEWER])], ERoles.ADMIN)).toBe(0);
    });
});
