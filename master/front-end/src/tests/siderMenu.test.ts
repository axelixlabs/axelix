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
import type { TFunction } from "i18next";
import { describe, expect, it } from "vitest";

import { EInstanceFeature } from "@/models";
import { getInstanceItems } from "@/utils";

const t = ((key: string) => key) as unknown as TFunction;

const DEPENDENCIES_PATH = "/instance/abc/dependencies";

const dependenciesEntry = (features: EInstanceFeature[] | null): { key: unknown; disabled?: boolean } | undefined => {
    const items = getInstanceItems("abc", t, features);
    const general = items.find((item) => item?.key === "general") as {
        children: { key: unknown; disabled?: boolean }[];
    };
    return general.children.find((child) => child.key === DEPENDENCIES_PATH);
};

describe("getInstanceItems dependency-analysis gating", () => {
    it("enables the Dependency Analyzer entry when the instance has the feature", () => {
        expect(dependenciesEntry([EInstanceFeature.DEPENDENCY_ANALYSIS])?.disabled).toBeFalsy();
    });

    it("greys out the Dependency Analyzer entry when the instance does not have the feature", () => {
        expect(dependenciesEntry([])?.disabled).toBe(true);
    });

    it("keeps the Dependency Analyzer entry enabled while the features are unknown", () => {
        expect(dependenciesEntry(null)?.disabled).toBeFalsy();
    });
});
