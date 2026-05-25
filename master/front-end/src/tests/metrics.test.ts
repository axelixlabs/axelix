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

import { getMetricTagValuesWithStatus, reduceDisplayedNumber } from "helpers";
import type { IValidTagCombination } from "models";
import { SHOW_RAW_THRESHOLD } from "utils";

describe("Check reduceDisplayedNumber function", () => {
    it("Returns an empty string if the value is null or undefined", () => {
        expect(reduceDisplayedNumber(null)).toBe("");
        expect(reduceDisplayedNumber(undefined)).toBe("");
    });

    it("Small integers below the threshold", () => {
        expect(reduceDisplayedNumber(42)).toBe("42");
        expect(reduceDisplayedNumber(-42)).toBe("-42");
        expect(reduceDisplayedNumber(SHOW_RAW_THRESHOLD - 1)).toBe(String(SHOW_RAW_THRESHOLD - 1));
    });

    it("Small fractional numbers below the threshold", () => {
        expect(reduceDisplayedNumber(12.3456)).toBe("12.34");
        expect(reduceDisplayedNumber(-12.34)).toBe("-12.34");
    });

    it("Thousands", () => {
        expect(reduceDisplayedNumber(1000)).toBe("1000");
        expect(reduceDisplayedNumber(2500)).toBe("2500");
        expect(reduceDisplayedNumber(-12500)).toBe("-12500");
    });

    it("Millions", () => {
        expect(reduceDisplayedNumber(1_000_000)).toBe("1M");
        expect(reduceDisplayedNumber(2_500_000)).toBe("2.5M");
    });

    it("Billions", () => {
        expect(reduceDisplayedNumber(1_000_000_000)).toBe("1B");
        expect(reduceDisplayedNumber(3_200_000_000)).toBe("3.2B");
    });

    it("Trillions", () => {
        expect(reduceDisplayedNumber(1_000_000_000_000)).toBe("1T");
        expect(reduceDisplayedNumber(7_500_000_000_000)).toBe("7.5T");
        expect(reduceDisplayedNumber(-12_345_678_901_234)).toBe("-12.35T");
    });

    it("Numbers at the threshold", () => {
        expect(reduceDisplayedNumber(SHOW_RAW_THRESHOLD)).toBe("100K");
        expect(reduceDisplayedNumber(SHOW_RAW_THRESHOLD - 0.001)).toBe(`${SHOW_RAW_THRESHOLD - 0.01}`);
    });
});

describe("Check getMetricTagValuesWithStatus function", () => {
    it("Returns empty array when there are no valid combinations", () => {
        const validTagCombinations: IValidTagCombination[] = [];
        const selectedTags: Record<string, string> = {};

        const result = getMetricTagValuesWithStatus(validTagCombinations, selectedTags);
        expect(result).toEqual([]);
    });

    it("No tags selected", () => {
        const validTagCombinations: IValidTagCombination[] = [
            {
                testKey1: "111",
                testKey2: "222",
            },
            {
                testKey1: "Test2",
                testKey2: "222",
            },
            {
                testKey1: "111",
                testKey2: "333",
            },
        ];
        const selectedTags: Record<string, string> = {};

        const result = getMetricTagValuesWithStatus(validTagCombinations, selectedTags);

        expect(result).toEqual([
            {
                tag: "testKey1",
                values: [
                    {
                        value: "111",
                        invalid: false,
                    },
                    {
                        value: "Test2",
                        invalid: false,
                    },
                ],
            },
            {
                tag: "testKey2",
                values: [
                    {
                        value: "222",
                        invalid: false,
                    },
                    {
                        value: "333",
                        invalid: false,
                    },
                ],
            },
        ]);
    });

    it("Some values enabled and some disabled when partial match exists", () => {
        const validTagCombinations: IValidTagCombination[] = [
            {
                TestKey1: "111",
                TestKey2: "111",
                TestKey3: "333",
            },
            {
                TestKey1: "111",
                TestKey2: "222",
                TestKey3: "222",
            },
            {
                TestKey1: "222",
                TestKey2: "222",
                TestKey3: "222",
            },
        ];

        const selectedTags = { TestKey2: "222" };

        const result = getMetricTagValuesWithStatus(validTagCombinations, selectedTags);

        expect(result).toEqual([
            {
                tag: "TestKey1",
                values: [
                    {
                        value: "111",
                        invalid: false,
                    },
                    {
                        value: "222",
                        invalid: false,
                    },
                ],
            },
            {
                tag: "TestKey2",
                values: [
                    {
                        value: "111",
                        invalid: false,
                    },
                    {
                        value: "222",
                        invalid: false,
                    },
                ],
            },
            {
                tag: "TestKey3",
                values: [
                    {
                        value: "222",
                        invalid: false,
                    },
                    {
                        value: "333",
                        invalid: true,
                    },
                ],
            },
        ]);
    });
});
