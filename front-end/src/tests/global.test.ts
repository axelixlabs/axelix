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

import { canonicalize } from "helpers";

describe("Canonicalize", () => {
    it("Converts a string to lowercase", () => {
        expect(canonicalize("HELLO")).toBe("hello");
        expect(canonicalize("HellO")).toBe("hello");
        expect(canonicalize("ПрИвЕт")).toBe("привет");
    });

    it("Removes spaces and punctuation marks", () => {
        expect(canonicalize("Hello, World!")).toBe("helloworld");
        expect(canonicalize("Привет, мир!")).toBe("приветмир");
    });

    it("Keeps only letters and digits", () => {
        expect(canonicalize("?A1-B2_C3:")).toBe("a1b2c3");
        expect(canonicalize("123-456.789_10")).toBe("12345678910");
    });

    it("Correctly handles already normalized strings", () => {
        expect(canonicalize("abc123")).toBe("abc123");
    });
});
