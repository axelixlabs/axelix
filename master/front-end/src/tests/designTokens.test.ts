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

import { applyDesignTokens, palette } from "@/theme/tokens";

const collectDesignTokens = (): Map<string, string> => {
    const properties = new Map<string, string>();
    applyDesignTokens({ setProperty: (name: string, value: string | null) => properties.set(name, value ?? "") });
    return properties;
};

describe("applyDesignTokens", () => {
    it("Exposes every palette shade as a --axelix-<hue>-<shade> variable", () => {
        const properties = collectDesignTokens();

        for (const [hue, shades] of Object.entries(palette)) {
            if (typeof shades === "string") {
                continue;
            }

            for (const [shade, color] of Object.entries(shades)) {
                expect(properties.get(`--axelix-${hue}-${shade}`)).toBe(color);
            }
        }
    });

    it("Exposes a single color as a --axelix-<name> variable", () => {
        const properties = collectDesignTokens();

        expect(properties.get("--axelix-white")).toBe("#fff");
    });

    it("Exposes the typography with CSS units", () => {
        const properties = collectDesignTokens();

        expect(properties.get("--axelix-font-family")).toBe("Golos, Helvetica, Arial, sans-serif");
        expect(properties.get("--axelix-font-size-body")).toBe("14px");
        expect(properties.get("--axelix-font-size-page-title")).toBe("24px");
        expect(properties.get("--axelix-line-height-normal")).toBe("1.5");
        expect(properties.get("--axelix-font-weight-semibold")).toBe("600");
    });
});
