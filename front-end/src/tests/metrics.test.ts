import { describe, expect, it } from "vitest";

import { extractUniqueTags, reduceDisplayedNumber } from "helpers";
import { SHOW_RAW_THRESHOLD } from "utils";

describe("reduceDisplayedNumber", () => {
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

describe("extractUniqueTags", () => {
    it("Should return an empty array on empty valid tag combinations", () => {
        expect(extractUniqueTags([])).toEqual([]);
    });
    it("Should handle case - single tag available", () => {
        expect(
            extractUniqueTags([
                {
                    area: "heap",
                },
                {
                    area: "nonheap",
                },
                {
                    area: "someOtherArea",
                },
            ]),
        ).toEqual(["area"]);
    });
    it("Should handle case - single tag with multiple values available", () => {
        expect(
            extractUniqueTags([
                {
                    area: "heap",
                },
                {
                    area: "nonheap",
                },
                {
                    area: "someOtherArea",
                },
            ]),
        ).toEqual(["area"]);
    });
    it("Should handle case - single tag single value available", () => {
        expect(
            extractUniqueTags([
                {
                    name: "dataSource",
                },
            ]),
        ).toEqual(["name"]);
    });
    it("Should handle case - two tags with multiple choices available", () => {
        expect(
            extractUniqueTags([
                {
                    area: "nonheap",
                    id: "Compressed Class Space",
                },
                {
                    area: "nonheap",
                    id: "CodeHeap 'non-profiled nmethods'",
                },
                {
                    area: "heap",
                    id: "G1 Eden Space",
                },
                {
                    area: "heap",
                    id: "G1 Survivor Space",
                },
                {
                    area: "nonheap",
                    id: "CodeHeap 'non-nmethods'",
                },
                {
                    area: "nonheap",
                    id: "Metaspace",
                },
                {
                    area: "nonheap",
                    id: "CodeHeap 'profiled nmethods'",
                },
                {
                    area: "heap",
                    id: "G1 Old Gen",
                },
            ]),
        ).toEqual(["area", "id"]);
    });
});
