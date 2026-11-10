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
import type { TFunction } from "i18next";
import { describe, expect, it } from "vitest";

import { filterScheduledTasks, formatInterval, getCronDescription } from "@/helpers";

const t = ((key: string) => key) as TFunction;

describe("getCronDescription", () => {
    it.each([
        ["en", "Every 3 seconds"],
        ["ru", "Каждые 3 секунды"],
    ])("Describes the cron expression in the %s language", (language, expected) => {
        // when.
        const result = getCronDescription("*/3 * * * * *", language, t);

        // then.
        expect(result).toBe(expected);
    });

    it("Returns the translated error when the expression cannot be described", () => {
        // when.
        const result = getCronDescription("not a cron", "ru", t);

        // then.
        expect(result).toBe("ScheduledTasks.editCronInvalidError");
    });
});

describe("formatInterval", () => {
    it.each([
        [500, "500 ms", "500 мс"],
        [2000, "2 sec", "2 с"],
        [1050, "1.05 sec", "1,05 с"],
        [60000, "1 min", "1 мин"],
        [90000, "1 min 30 sec", "1 мин 30 с"],
        [3600000, "1 hr", "1 ч"],
        [86400000, "24 hr", "24 ч"],
    ])("Formats %d milliseconds", (intervalMillis, english, russian) => {
        // when.
        const englishResult = formatInterval(intervalMillis, "en").replace(/\s/g, " ");
        const russianResult = formatInterval(intervalMillis, "ru").replace(/\s/g, " ");

        // then.
        expect(englishResult).toBe(english);
        expect(russianResult).toBe(russian);
    });
});

describe("filterScheduledTasks", () => {
    it.each([
        ["heartbeat", true],
        ["Scheduler", true],
        ["com.example.Scheduler.heartbeat", true],
        ["  HEARTBEAT  ", true],
        ["", true],
        ["unrelated", false],
        ["opaque-id", false],
    ])("Filters by class and method names for search '%s'", (search, matches) => {
        // given.
        const task = {
            taskId: "opaque-id",
            className: "com.example.Scheduler",
            methodName: "heartbeat",
            enabled: true,
            successRate: null,
        };
        const cron = { ...task, expression: "*/3 * * * * *" };
        const fixed = { ...task, interval: 1000, initialDelay: 0 };
        const tasks = { cron: [cron], fixedDelay: [fixed], fixedRate: [fixed] };

        // when.
        const result = filterScheduledTasks(tasks, search);

        // then.
        expect(result).toEqual(matches ? tasks : { cron: [], fixedDelay: [], fixedRate: [] });
    });
});
