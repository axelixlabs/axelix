import { describe, expect, it } from "vitest";

import { extractUniqueMetricValuesPerKey, reduceDisplayedNumber } from "helpers";
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

const validTagCombinations: IValidTagCombination[] = [
    {
        a: "1",
        b: "1",
        c: "3",
    },
    {
        a: "1",
        b: "1",
        c: "4",
    },
    {
        a: "1",
        b: "2",
        c: "2",
    },
    {
        a: "1",
        b: "2",
        c: "3",
    },
    {
        a: "2",
        b: "2",
        c: "2",
    },
];

describe("Check extractUniqueMetricValuesPerKey function", () => {
    it("Returns all unique values for each key when no selected keys are provided", () => {
        const uniqueMetricValuesPerKey = extractUniqueMetricValuesPerKey(validTagCombinations, {});

        expect(uniqueMetricValuesPerKey).toEqual([
            {
                tag: "a",
                values: ["1", "2"],
            },
            {
                tag: "b",
                values: ["1", "2"],
            },
            {
                tag: "c",
                values: ["3", "4", "2"],
            },
        ]);
    });

    it("Filters combinations by single selected tag", () => {
        const selectedTags = { a: "1" };
        const uniqueMetricValuesPerKey = extractUniqueMetricValuesPerKey(validTagCombinations, selectedTags);

        expect(uniqueMetricValuesPerKey).toEqual([
            {
                tag: "a",
                values: ["1"],
            },
            {
                tag: "b",
                values: ["1", "2"],
            },
            {
                tag: "c",
                values: ["3", "4", "2"],
            },
        ]);
    });

    it("Filters by multiple selected tags", () => {
        const selectedTags = { a: "1", b: "2" };
        const uniqueMetricValuesPerKey = extractUniqueMetricValuesPerKey(validTagCombinations, selectedTags);

        expect(uniqueMetricValuesPerKey).toEqual([
            {
                tag: "a",
                values: ["1"],
            },
            {
                tag: "b",
                values: ["2"],
            },
            {
                tag: "c",
                values: ["2", "3"],
            },
        ]);
    });
});
