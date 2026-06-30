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
import {
    ETimepickerHourCycle,
    type ILogger,
    type ILoggerGroup,
    type ILoggersResponseBody,
    type ITimepickerClockConfig,
    type ITimepickerData,
} from "models";
import { SECONDS_IN_HOUR, SECONDS_IN_MINUTE } from "utils";

export const mapLoggersResponse = (response: ILoggersResponseBody) => {
    const { levels, loggers, groups } = response;
    const loggerEntries = Object.entries(loggers);
    const loggerGroupEntries = Object.entries(groups);

    const mappedLoggers = loggerEntries.map(([name, logger]) => {
        return {
            name: name,
            ...logger,
        };
    });

    const mappedGroups = loggerGroupEntries.map(([name, group]) => {
        return {
            name: name,
            ...group,
        };
    });

    return {
        levels: levels,
        loggers: mappedLoggers,
        groups: mappedGroups,
    };
};

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
            locale: locale || "en-US",
        };
    } catch {
        return {
            type: "24h",
            locale: "en-US",
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

    if (type === ETimepickerHourCycle.AM && parsedHour === 12) {
        parsedHour = 0;
    } else if (type === ETimepickerHourCycle.PM && parsedHour !== 12) {
        parsedHour += 12;
    }

    return parsedHour * SECONDS_IN_HOUR + parsedMinutes * SECONDS_IN_MINUTE;
};
