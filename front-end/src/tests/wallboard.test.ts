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
import { beforeEach, describe, expect, it } from "vitest";

import { createWallboardFilterId, removeFilterById, semVerMatch } from "helpers";
import { EWallboardFilterKey, EWallboardFilterOperator, type IWallboardSingleOperandFilter } from "models";
import { SEARCH_PARAMS_FILTER } from "utils";

const createFilter = (operator: EWallboardFilterOperator, operand: string): IWallboardSingleOperandFilter => ({
    id: "test-filter",
    key: EWallboardFilterKey.SPRING_BOOT,
    operator,
    operand,
});

describe("semVerMatch: EQUAL", () => {
    it("returns true when full semver matches exactly", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "1.2.3");

        // when.
        const result = semVerMatch("1.2.3", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns false when full semver does not match", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "1.2.3");

        // when.
        const result = semVerMatch("1.2.4", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when filter has major only and major matches", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "3");

        // when.
        const result = semVerMatch("3.5.7", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns false when filter has major only and major does not match", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "3");

        // when.
        const result = semVerMatch("4.0.0", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when filter has major.minor and both match", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "2.5");

        // when.
        const result = semVerMatch("2.5.9", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when filter has major.minor.patch and all match", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "2.5.9");

        // when.
        const result = semVerMatch("2.5.9", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns false when filter has major.minor and minor does not match", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "2.5");

        // when.
        const result = semVerMatch("2.6.0", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns false when filter has major.minor and major does not match", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "2.5");

        // when.
        const result = semVerMatch("3.5.0", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when candidate has major only and filter major matches", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "5");

        // when.
        const result = semVerMatch("5", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when both have major.minor only and they match", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "3.2");

        // when.
        const result = semVerMatch("3.2", filter);

        // then.
        expect(result).toBe(true);
    });

    it("filters Spring Boot 2.7.x exactly", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.EQUAL, "2.7");

        // when/then.
        expect(semVerMatch("2.7.0", filter)).toBe(true);
        expect(semVerMatch("2.7.18", filter)).toBe(true);
        expect(semVerMatch("2.6.15", filter)).toBe(false);
        expect(semVerMatch("3.0.0", filter)).toBe(false);
    });
});

describe("semVerMatch: GREATER_THAN_EQUAL", () => {
    it("returns true when candidate major > filter major (5.0.0 >= 4)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "4");

        // when.
        const result = semVerMatch("5.0.0", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate major > filter major with full versions (5.0.0 >= 4.9.9)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "4.9.9");

        // when.
        const result = semVerMatch("5.0.0", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate major < filter major but minor > filterMinor", () => {
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "5.0.0");

        // when.
        const result = semVerMatch("4.9.9", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when candidate major < filter major and filter has major only", () => {
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "5");

        // when.
        const result = semVerMatch("4.9.9", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when filter has major only (5.2.3 >= 5)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "5");

        // when.
        const result = semVerMatch("5.2.3", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when filter has major only and candidate has major only (5 >= 5)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "5");

        // when.
        const result = semVerMatch("5", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate has major only but filter has minor", () => {
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "7.5");

        // when.
        const result = semVerMatch("7", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when candidate minor > filter minor (7.7.0 >= 7.5)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "7.5");

        // when.
        const result = semVerMatch("7.7.0", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate minor < filter minor but filterPatch is undefined", () => {
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "7.5");

        // when.
        const result = semVerMatch("7.4.9", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when candidate minor < filter minor and filter has patch", () => {
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "7.5.0");

        // when.
        const result = semVerMatch("7.4.9", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when filter has major.minor only (7.2.8 >= 7.2)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "7.2");

        // when.
        const result = semVerMatch("7.2.8", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when filter has major.minor only and candidate has major.minor only (7.2 >= 7.2)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "7.2");

        // when.
        const result = semVerMatch("7.2", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate has major.minor only but filter has patch", () => {
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "7.2.4");

        // when.
        const result = semVerMatch("7.2", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when candidate patch > filter patch (7.2.8 >= 7.2.4)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "7.2.4");

        // when.
        const result = semVerMatch("7.2.8", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate patch < filter patch", () => {
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "7.2.4");

        // when.
        const result = semVerMatch("7.2.3", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when candidate patch == filter patch (7.2.4 >= 7.2.4)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "7.2.4");

        // when.
        const result = semVerMatch("7.2.4", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true for 0.0.0 >= 0.0.0", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "0.0.0");

        // when.
        const result = semVerMatch("0.0.0", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate is much larger (100.50.25 >= 1.0.0)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "1.0.0");

        // when.
        const result = semVerMatch("100.50.25", filter);

        // then.
        expect(result).toBe(true);
    });

    it("filters Spring Boot 3.x and above - has bugs due to filterMinor being undefined", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.GREATER_THAN_EQUAL, "3");

        // when/then.
        expect(semVerMatch("3.0.0", filter)).toBe(true);
        expect(semVerMatch("3.1.5", filter)).toBe(true);
        expect(semVerMatch("3.2.0", filter)).toBe(true);
        expect(semVerMatch("4.0.0", filter)).toBe(true);
        expect(semVerMatch("2.7.18", filter)).toBe(false);
        expect(semVerMatch("2.0.0", filter)).toBe(false);
    });
});

describe("semVerMatch: LESS_THAN_EQUAL", () => {
    it("returns true when candidate major < filter major (6.0.0 <= 7)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "7");

        // when.
        const result = semVerMatch("6.0.0", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate major < filter major with full versions (6.9.9 <= 7.0.0)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "7.0.0");

        // when.
        const result = semVerMatch("6.9.9", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate major > filter major", () => {
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6.0.0");

        // when.
        const result = semVerMatch("7.0.0", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when filter has major only and candidate has minor", () => {
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6");

        // when.
        const result = semVerMatch("6.1.0", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when filter has major only and candidate has major only (6 <= 6)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6");

        // when.
        const result = semVerMatch("6", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate has major only but filter has minor (6 <= 6.2)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6.2");

        // when.
        const result = semVerMatch("6", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate minor < filter minor (6.1.0 <= 6.2)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6.2");

        // when.
        const result = semVerMatch("6.1.0", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate minor > filter minor", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6.2");

        // when.
        const result = semVerMatch("6.3.0", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when filter has major.minor only and candidate has patch", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6.2");

        // when.
        const result = semVerMatch("6.2.1", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true when filter has major.minor only and candidate has major.minor only (6.2 <= 6.2)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6.2");

        // when.
        const result = semVerMatch("6.2", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate has major.minor only but filter has patch (6.2 <= 6.2.2)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6.2.2");

        // when.
        const result = semVerMatch("6.2", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate patch < filter patch (6.2.1 <= 6.2.2)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6.2.2");

        // when.
        const result = semVerMatch("6.2.1", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate patch == filter patch (6.2.2 <= 6.2.2)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6.2.2");

        // when.
        const result = semVerMatch("6.2.2", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate patch > filter patch - BUG (6.2.3 <= 6.2.2)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "6.2.2");

        // when.
        const result = semVerMatch("6.2.3", filter);

        // then.
        expect(result).toBe(false);
    });

    it("returns true for 0.0.0 <= 0.0.0", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "0.0.0");

        // when.
        const result = semVerMatch("0.0.0", filter);

        // then.
        expect(result).toBe(true);
    });

    it("returns true when candidate is much smaller (1.0.0 <= 100.50.25)", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "100.50.25");

        // when.
        const result = semVerMatch("1.0.0", filter);

        // then.
        expect(result).toBe(true);
    });

    it("filters older Spring Boot versions - has bugs due to filterPatch being undefined", () => {
        // given.
        const filter = createFilter(EWallboardFilterOperator.LESS_THAN_EQUAL, "2.6");

        // when/then.
        expect(semVerMatch("2.5.15", filter)).toBe(true);
        expect(semVerMatch("2.6", filter)).toBe(true);
        expect(semVerMatch("2.6.0", filter)).toBe(true);
        expect(semVerMatch("2.7.0", filter)).toBe(false);
        expect(semVerMatch("3.1.0", filter)).toBe(false);
        expect(semVerMatch("1.5.22", filter)).toBe(true);
    });
});

describe("semVerMatch: unknown operator", () => {
    it("returns true for unknown operator", () => {
        // given.
        const filter = {
            id: "test-filter",
            key: EWallboardFilterKey.SPRING_BOOT,
            operator: "!=" as unknown as EWallboardFilterOperator,
            operand: "1.2.3",
        };

        // when.
        const result = semVerMatch("1.2.3", filter);

        // then.
        expect(result).toBe(true);
    });
});

describe("removeFilterById", () => {
    const javaFilter = `${EWallboardFilterKey.JAVA}:${EWallboardFilterOperator.EQUAL}:17`;
    const springBootFilter = `${EWallboardFilterKey.SPRING_BOOT}:${EWallboardFilterOperator.GREATER_THAN_EQUAL}:2.7`;

    let searchParams: URLSearchParams;

    beforeEach(() => {
        searchParams = new URLSearchParams();
    });

    it("Should remove filter by id - happy path", () => {
        // given.
        searchParams.append(SEARCH_PARAMS_FILTER, javaFilter);
        searchParams.append(SEARCH_PARAMS_FILTER, springBootFilter);

        const filterIdForRemove = createWallboardFilterId(
            EWallboardFilterKey.JAVA,
            EWallboardFilterOperator.EQUAL,
            "17",
        );

        // when.
        removeFilterById(searchParams, filterIdForRemove);

        // then.
        expect(searchParams.getAll(SEARCH_PARAMS_FILTER)).toEqual([springBootFilter]);
    });

    it("Should handle invalid filter id", () => {
        // given.
        searchParams.append(SEARCH_PARAMS_FILTER, javaFilter);
        searchParams.append(SEARCH_PARAMS_FILTER, springBootFilter);

        // when.
        removeFilterById(searchParams, "random_id");

        // then.
        expect(searchParams.getAll(SEARCH_PARAMS_FILTER)).toEqual([javaFilter, springBootFilter]);
    });

    it("Should handle if absent filter id removal requested", () => {
        // given.
        searchParams.append(SEARCH_PARAMS_FILTER, javaFilter);
        searchParams.append(SEARCH_PARAMS_FILTER, springBootFilter);

        // and.
        const java21FilterId = createWallboardFilterId(EWallboardFilterKey.JAVA, EWallboardFilterOperator.EQUAL, "21");

        // when.
        removeFilterById(searchParams, java21FilterId);

        // then.
        expect(searchParams.getAll(SEARCH_PARAMS_FILTER)).toEqual([javaFilter, springBootFilter]);
    });

    it("Should return empty array if no filters found", () => {
        // given.
        searchParams.append("random_filter", "random_value");

        // when.
        removeFilterById(searchParams, "random_id");

        // then.
        expect(searchParams.getAll(SEARCH_PARAMS_FILTER)).toEqual([]);
    });
});
