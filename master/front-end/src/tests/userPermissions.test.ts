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

import { ERoles } from "@/models";
import { effectiveAuthorities } from "@/pages/UserProfile/permissions";

describe("effectiveAuthorities", () => {
    it("grants an editor the runtime operations and nothing sensitive", () => {
        const granted = effectiveAuthorities([ERoles.EDITOR]);

        expect(granted.has("CACHES_CLEAR")).toBe(true);
        expect(granted.has("ENV_VALUES_READ")).toBe(false);
    });

    it("grants every authority held by any of the user's roles", () => {
        const granted = effectiveAuthorities([ERoles.EDITOR, ERoles.ADMIN]);

        expect(granted.has("SCHEDULED_TASKS_MODIFY")).toBe(true);
        expect(granted.has("CONFIG_PROPS_VALUES_READ")).toBe(true);
    });

    it("grants nothing to a viewer", () => {
        expect(effectiveAuthorities([ERoles.VIEWER]).size).toBe(0);
    });

    it("ignores a role the built-in grant map does not know", () => {
        expect(effectiveAuthorities(["CUSTOM"]).size).toBe(0);
    });
});
