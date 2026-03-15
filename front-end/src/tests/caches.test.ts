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

import { buildChartData } from "helpers";
import { ELookupOutcome, type ICacheLookup } from "models";

const hit = (timestamp = 0): ICacheLookup => ({ timestamp, outcome: ELookupOutcome.HIT });
const miss = (timestamp = 0): ICacheLookup => ({ timestamp, outcome: ELookupOutcome.MISS });

const ratios = (lookupHistory: ICacheLookup[], slidingWindow: number): number[] =>
    buildChartData(lookupHistory, slidingWindow).map((dp) => dp.count);

describe("buildChartData", () => {
    it("Returns an empty array for empty lookup history", () => {
        // given.
        const lookupHistory: ICacheLookup[] = [];

        // when.
        const result = buildChartData(lookupHistory, 5);

        // then.
        expect(result).toEqual([]);
    });

    it("Returns [1] for a single HIT", () => {
        // given.
        const lookupHistory = [hit()];

        // when.
        const result = ratios(lookupHistory, 3);

        // then.
        expect(result).toEqual([1]);
    });

    it("Returns [0] for a single MISS", () => {
        // given.
        const lookupHistory = [miss()];

        // when.
        const result = ratios(lookupHistory, 3);

        // then.
        expect(result).toEqual([0]);
    });

    it("All HITs produce all 1s regardless of sliding window", () => {
        // given.
        const lookupHistory = [hit(), hit(), hit(), hit(), hit()];

        // when.
        const result = ratios(lookupHistory, 3);

        // then.
        expect(result).toEqual([1, 1, 1, 1, 1]);
    });

    it("All MISSes produce all 0s regardless of sliding window", () => {
        // given.
        const lookupHistory = [miss(), miss(), miss(), miss(), miss()];

        // when.
        const result = ratios(lookupHistory, 3);

        // then.
        expect(result).toEqual([0, 0, 0, 0, 0]);
    });

    it("Warmup phase uses cumulative ratio when history is shorter than the window", () => {
        // given.
        const lookupHistory = [hit(), miss(), hit()];

        // when.
        const result = ratios(lookupHistory, 10);

        // then.
        expect(result).toEqual([1, 1 / 2, 2 / 3]);
    });

    it("Sliding window of size 1 returns 1 for HIT and 0 for MISS at each position", () => {
        // given.
        const lookupHistory = [hit(), miss(), hit(), miss(), hit()];

        // when.
        const result = ratios(lookupHistory, 1);

        // then.
        expect(result).toEqual([1, 0, 1, 0, 1]);
    });

    it("Correctly slides the window, evicting the oldest element", () => {
        // given.
        // Window size 3: [H, H, M] -> [H, M, H] -> [M, H, M] -> [H, M, M]
        const lookupHistory = [hit(), hit(), miss(), hit(), miss(), miss()];

        // when.
        const result = ratios(lookupHistory, 3);

        // then.
        expect(result).toEqual([
            1 / 1, // warmup: 1 hit / 1 total
            2 / 2, // warmup: 2 hits / 2 total
            2 / 3, // warmup complete: [H, H, M] -> 2 hits
            2 / 3, // slide: add H, evict H -> [H, M, H] -> 2 hits
            1 / 3, // slide: add M, evict H -> [M, H, M] -> 1 hit
            1 / 3, // slide: add M, evict M -> [H, M, M] -> 1 hit
        ]);
    });

    it("Evicting a MISS from the tail does not decrement the hit counter", () => {
        // given.
        // Window size 2: [M, H] then add H -> evict M (a MISS), window becomes [H, H]
        const lookupHistory = [miss(), hit(), hit()];

        // when.
        const result = ratios(lookupHistory, 2);

        // then.
        expect(result).toEqual([
            0 / 1, // warmup: 0 hits / 1 total
            1 / 2, // warmup: 1 hit / 2 total (window full: [M, H])
            2 / 2, // window [H, H]: evict M, add H -> 2 hits
        ]);
    });

    it("Alternating hits and misses with window size 2", () => {
        // given.
        const lookupHistory = [hit(), miss(), hit(), miss(), hit(), miss()];

        // when.
        const result = ratios(lookupHistory, 2);

        // then.
        expect(result).toEqual([
            1 / 1, // warmup: [H]
            1 / 2, // warmup: [H, M]
            1 / 2, // slide: add H, evict H -> [M, H] -> 1 hit
            1 / 2, // slide: add M, evict M -> [H, M] -> 1 hit
            1 / 2, // slide: add H, evict H -> [M, H] -> 1 hit
            1 / 2, // slide: add M, evict M -> [H, M] -> 1 hit
        ]);
    });

    it("Window size equal to the history length means everything is in the warmup phase", () => {
        // given.
        const lookupHistory = [hit(), miss(), hit(), miss()];

        // when.
        const result = ratios(lookupHistory, 4);

        // then.
        expect(result).toEqual([1 / 1, 1 / 2, 2 / 3, 2 / 4]);
    });

    it("Window size larger than the history length keeps everything in the warmup phase", () => {
        // given.
        const lookupHistory = [miss(), hit(), hit()];

        // when.
        const result = ratios(lookupHistory, 100);

        // then.
        expect(result).toEqual([0 / 1, 1 / 2, 2 / 3]);
    });

    it("Each data point carries the timestamp from its corresponding lookup", () => {
        // given.
        const lookupHistory = [hit(1000), miss(2000), hit(3000)];

        // when.
        const result = buildChartData(lookupHistory, 2);

        // then.
        expect(result).toEqual([
            { timestamp: 1000, count: 1 },
            { timestamp: 2000, count: 1 / 2 },
            { timestamp: 3000, count: 1 / 2 },
        ]);
    });
});
