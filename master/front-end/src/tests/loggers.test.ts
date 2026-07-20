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
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { timepickerDataConvertToSeconds } from "helpers";
import { ETimepickerHourCycle } from "models";

const HOUR_IN_SECONDS = 60 * 60;
const DAY_IN_SECONDS = 24 * HOUR_IN_SECONDS;

describe("Check timepickerDataConvertToSeconds function", () => {
    beforeEach(() => {
        // Freeze "now" at a fixed local wall-clock time so the diff is deterministic
        // regardless of the timezone the test runs in. Mid-January avoids any DST shift
        // between "now" and the same time on the next day.
        vi.useFakeTimers();
        vi.setSystemTime(new Date(2026, 0, 15, 10, 0, 0, 0));
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it("Returns 0 when data is undefined", () => {
        // given.
        const data = undefined;

        // when.
        const result = timepickerDataConvertToSeconds(data);

        // then.
        expect(result).toBe(0);
    });

    it("24h format: returns the seconds until a time later today", () => {
        // given.
        const data = { hour: "14", minutes: "30" };

        // when.
        const result = timepickerDataConvertToSeconds(data);

        // then.
        expect(result).toBe(4 * HOUR_IN_SECONDS + 30 * 60);
    });

    it("24h format: rolls over to the next day when the time has already passed today", () => {
        // given.
        const data = { hour: "8", minutes: "0" };

        // when.
        const result = timepickerDataConvertToSeconds(data);

        // then.
        expect(result).toBe(22 * HOUR_IN_SECONDS);
    });

    it("24h format: rolls over to the next day when the time is exactly now", () => {
        // given.
        const data = { hour: "10", minutes: "0" };

        // when.
        const result = timepickerDataConvertToSeconds(data);

        // then.
        expect(result).toBe(DAY_IN_SECONDS);
    });

    it("12h format: treats 12 AM as midnight", () => {
        // given.
        const data = { hour: "12", minutes: "0", type: ETimepickerHourCycle.AM };

        // when.
        const result = timepickerDataConvertToSeconds(data);

        // then. midnight (00:00) already passed today, so it rolls to the next day
        expect(result).toBe(14 * HOUR_IN_SECONDS);
    });

    it("12h format: keeps a non-12 AM hour as-is", () => {
        // given.
        const data = { hour: "9", minutes: "30", type: ETimepickerHourCycle.AM };

        // when.
        const result = timepickerDataConvertToSeconds(data);

        // then. 09:30 already passed today, so it rolls to the next day
        expect(result).toBe(23 * HOUR_IN_SECONDS + 30 * 60);
    });

    it("12h format: shifts a non-12 PM hour by 12 hours", () => {
        // given.
        const data = { hour: "3", minutes: "0", type: ETimepickerHourCycle.PM };

        // when.
        const result = timepickerDataConvertToSeconds(data);

        // then. 15:00 is later today
        expect(result).toBe(5 * HOUR_IN_SECONDS);
    });

    it("12h format: treats 12 PM as noon", () => {
        // given.
        const data = { hour: "12", minutes: "0", type: ETimepickerHourCycle.PM };

        // when.
        const result = timepickerDataConvertToSeconds(data);

        // then. 12:00 is later today
        expect(result).toBe(2 * HOUR_IN_SECONDS);
    });
});
