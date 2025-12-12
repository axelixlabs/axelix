/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                        disabled: false,
                    },
                    {
                        value: "Test2",
                        disabled: false,
                    },
                ],
            },
            {
                tag: "testKey2",
                values: [
                    {
                        value: "222",
                        disabled: false,
                    },
                    {
                        value: "333",
                        disabled: false,
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
                        disabled: false,
                    },
                    {
                        value: "222",
                        disabled: false,
                    },
                ],
            },
            {
                tag: "TestKey2",
                values: [
                    {
                        value: "111",
                        disabled: false,
                    },
                    {
                        value: "222",
                        disabled: false,
                    },
                ],
            },
            {
                tag: "TestKey3",
                values: [
                    {
                        value: "222",
                        disabled: false,
                    },
                    {
                        value: "333",
                        disabled: true,
                    },
                ],
            },
        ]);
    });
});
