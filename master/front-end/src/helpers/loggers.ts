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
import dayjs from "dayjs";
import isSameOrBefore from "dayjs/plugin/isSameOrBefore";

import {
    ETimepickerHourCycle,
    type ILogger,
    type ILoggerGroup,
    type ITimepickerClockConfig,
    type ITimepickerData,
} from "models";
import { DEFAULT_TIME_LOCALE } from "utils";

dayjs.extend(isSameOrBefore);

export const filterLoggers = (loggers: ILogger[], search: string): ILogger[] => {
    const formattedSearch = search.toLowerCase().trim();

    return loggers.filter(({ name }) => name.toLowerCase().includes(formattedSearch));
};

export const filterLoggerGroups = (loggerGroups: ILoggerGroup[], search: string): ILoggerGroup[] => {
    const formattedSearch = search.toLowerCase().trim();

    return loggerGroups.reduce<ILoggerGroup[]>((result, loggerGroup) => {
        const { name, members } = loggerGroup;

        const loggerGroupNameLower = name.toLowerCase();

        if (loggerGroupNameLower.includes(formattedSearch)) {
            result.push(loggerGroup);
            return result;
        }

        const anyLoggerMatches = members.some((member) => member.toLowerCase().includes(formattedSearch));

        if (anyLoggerMatches) {
            result.push(loggerGroup);
            return result;
        }
        return result;
    }, []);
};

export const getTimepickerClockConfig = (): ITimepickerClockConfig => {
    try {
        const formatter = new Intl.DateTimeFormat(undefined, {
            hour: "numeric",
        });

        const { hourCycle, locale } = formatter.resolvedOptions();

        let type: "12h" | "24h";

        if (hourCycle === "h23" || hourCycle === "h24") {
            type = "24h";
        } else {
            type = "12h";
        }

        return {
            type: type,
            locale: locale,
        };
    } catch {
        return {
            type: "24h",
            locale: DEFAULT_TIME_LOCALE,
        };
    }
};

export const timepickerDataConvertToSeconds = (data: ITimepickerData | undefined): number => {
    if (!data) {
        return 0;
    }

    const { hour, minutes, type } = data;
    let parsedHour = Number(hour);
    const parsedMinutes = Number(minutes);

    const isAM = type === ETimepickerHourCycle.AM;
    const isPM = type === ETimepickerHourCycle.PM;

    if (isAM && parsedHour === 12) {
        parsedHour = 0;
    } else if (isPM && parsedHour !== 12) {
        parsedHour += 12;
    }

    const now = dayjs();
    let target = now.hour(parsedHour).minute(parsedMinutes).second(0).millisecond(0);

    if (target.isSameOrBefore(now)) {
        target = target.add(1, "day");
    }

    return target.diff(now, "second");
};
