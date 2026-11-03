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
import { authorityDelta, effectiveAuthorities } from "@/pages/UserProfile/permissions";

describe("effectiveAuthorities", () => {
    it("grants an editor the runtime operations and nothing sensitive", () => {
        const granted = effectiveAuthorities([ERoles.EDITOR]);

        expect(granted.get("CACHES_CLEAR")).toBe(ERoles.EDITOR);
        expect(granted.has("ENV_VALUES_READ")).toBe(false);
    });

    it("attributes a shared authority to ADMIN when both roles are held", () => {
        const granted = effectiveAuthorities([ERoles.EDITOR, ERoles.ADMIN]);

        expect(granted.get("SCHEDULED_TASKS_MODIFY")).toBe(ERoles.ADMIN);
        expect(granted.get("CONFIG_PROPS_VALUES_READ")).toBe(ERoles.ADMIN);
    });

    it("grants nothing to a viewer", () => {
        expect(effectiveAuthorities([ERoles.VIEWER]).size).toBe(0);
    });
});

describe("authorityDelta", () => {
    it("reports the sensitive values gained by adding ADMIN", () => {
        const delta = authorityDelta([ERoles.EDITOR], [ERoles.EDITOR, ERoles.ADMIN]);

        expect(delta.gained).toEqual(["ENV_VALUES_READ", "CONFIG_PROPS_VALUES_READ"]);
        expect(delta.lost).toEqual([]);
        expect(delta.retainedThrough).toBeNull();
    });

    it("keeps the editor operations when ADMIN is revoked", () => {
        const delta = authorityDelta([ERoles.EDITOR, ERoles.ADMIN], [ERoles.EDITOR]);

        expect(delta.lost).toEqual(["ENV_VALUES_READ", "CONFIG_PROPS_VALUES_READ"]);
        expect(delta.gained).toEqual([]);
        expect(delta.retainedThrough).toBe(ERoles.EDITOR);
    });
});
