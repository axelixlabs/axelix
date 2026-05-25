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

import { formatTransactionDuration } from "helpers";

describe("Check formatTransactionDuration function", () => {
    it("Returns milliseconds for values below 1000", () => {
        expect(formatTransactionDuration(0)).toBe("0 ms");
        expect(formatTransactionDuration(1)).toBe("1 ms");
        expect(formatTransactionDuration(500)).toBe("500 ms");
        expect(formatTransactionDuration(999)).toBe("999 ms");
    });

    it("Returns seconds for values equal to 1000", () => {
        expect(formatTransactionDuration(1000)).toBe("1s");
    });

    it("Returns whole seconds without decimals for exact second values", () => {
        expect(formatTransactionDuration(2000)).toBe("2s");
        expect(formatTransactionDuration(5000)).toBe("5s");
        expect(formatTransactionDuration(10000)).toBe("10s");
    });

    it("Returns seconds with one decimal place for fractional second values", () => {
        expect(formatTransactionDuration(1500)).toBe("1.5s");
        expect(formatTransactionDuration(2300)).toBe("2.3s");
        expect(formatTransactionDuration(12750)).toBe("12.8s");
    });

    it("Handles the boundary between milliseconds and seconds", () => {
        expect(formatTransactionDuration(999)).toBe("999 ms");
        expect(formatTransactionDuration(1000)).toBe("1s");
        expect(formatTransactionDuration(1001)).toBe("1.0s");
    });
});
