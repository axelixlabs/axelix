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
import type {
    ICacheData,
    ICacheLookup,
    ICachesManager,
    IGetSingleCacheResponseBody,
    IGroupTimestampEvent,
} from "models";

import {
    SINGLE_CACHE_CHART_TIMELINE_STEP_5M,
    SINGLE_CACHE_CHART_TIMELINE_STEP_5S,
    SINGLE_CACHE_CHART_TIMELINE_STEP_15M,
    SINGLE_CACHE_CHART_TIMELINE_STEP_30D,
    SINGLE_CACHE_CHART_TIMELINE_STEP_D,
    SINGLE_CACHE_CHART_TIMELINE_STEP_H,
    SINGLE_CACHE_CHART_TIMELINE_STEP_M,
    SINGLE_CACHE_CHART_TIMELINE_STEP_S,
} from "../utils";

export const filterCacheManagers = (cacheManager: ICachesManager[], search: string): ICachesManager[] => {
    const formattedSearch = search.toLowerCase().trim();

    return cacheManager.filter(({ name, caches }) => {
        const lowerName = name.toLowerCase();
        if (lowerName.includes(formattedSearch)) {
            return true;
        }

        return caches.some(({ name: cacheName }) => cacheName.toLowerCase().includes(formattedSearch));
    });
};

export const getTimelineInterval = (data: IGetSingleCacheResponseBody): number => {
    const allTimestamps = [
        ...data.hits.map(({ timestamp }) => timestamp),
        ...data.misses.map(({ timestamp }) => timestamp),
    ];

    if (allTimestamps.length === 0) {
        return SINGLE_CACHE_CHART_TIMELINE_STEP_S;
    }

    // TODO: This can be optimized since the hits and misses are guaranteed to be sorted in
    // response from the backend
    const range = Math.max(...allTimestamps) - Math.min(...allTimestamps);

    if (range <= SINGLE_CACHE_CHART_TIMELINE_STEP_M) {
        return SINGLE_CACHE_CHART_TIMELINE_STEP_S;
    }

    if (range <= 10 * SINGLE_CACHE_CHART_TIMELINE_STEP_M) {
        return SINGLE_CACHE_CHART_TIMELINE_STEP_5S;
    }

    if (range <= SINGLE_CACHE_CHART_TIMELINE_STEP_H) {
        return SINGLE_CACHE_CHART_TIMELINE_STEP_M;
    }

    if (range <= 6 * SINGLE_CACHE_CHART_TIMELINE_STEP_H) {
        return SINGLE_CACHE_CHART_TIMELINE_STEP_5M;
    }

    if (range <= SINGLE_CACHE_CHART_TIMELINE_STEP_D) {
        return SINGLE_CACHE_CHART_TIMELINE_STEP_15M;
    }

    if (range <= 7 * SINGLE_CACHE_CHART_TIMELINE_STEP_D) {
        return SINGLE_CACHE_CHART_TIMELINE_STEP_H;
    }

    if (range <= 30 * SINGLE_CACHE_CHART_TIMELINE_STEP_D) {
        return SINGLE_CACHE_CHART_TIMELINE_STEP_D;
    }

    return SINGLE_CACHE_CHART_TIMELINE_STEP_30D;
};

export const cacheHitsMissesChartToFormattedTime = (value: number, interval: number): string => {
    const date = new Date(value);

    if (interval <= SINGLE_CACHE_CHART_TIMELINE_STEP_5S) {
        return date.toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
        });
    }

    if (interval <= SINGLE_CACHE_CHART_TIMELINE_STEP_15M) {
        return date.toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
        });
    }

    if (interval === SINGLE_CACHE_CHART_TIMELINE_STEP_H) {
        return date.toLocaleString([], {
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
        });
    }

    if (interval === SINGLE_CACHE_CHART_TIMELINE_STEP_D) {
        return date.toLocaleDateString([], {
            day: "2-digit",
            month: "2-digit",
        });
    }

    return date.toLocaleDateString([], {
        month: "2-digit",
        year: "2-digit",
    });
};

/**
 * Split passed caches into two parts - caches that are supposed to have the drop-down and those that do not.
 */
export const splitCaches = (caches: ICacheData[]): [ICacheData[], ICacheData[]] => {
    const withDropDown: ICacheData[] = [];
    const withoutDropDown: ICacheData[] = [];

    caches.forEach((cache) => {
        if (cache.containsStats) {
            withDropDown.push(cache);
        } else {
            withoutDropDown.push(cache);
        }
    });

    return [withDropDown, withoutDropDown];
};

const getTimestampCountsMap = (events: ICacheLookup[]): Record<number, number> => {
    const timestampCountsMap: Record<number, number> = {};
    for (const { timestamp } of events) {
        timestampCountsMap[timestamp] = (timestampCountsMap[timestamp] ?? 0) + 1;
    }
    return timestampCountsMap;
};

const groupTimestampsEvents = (events: ICacheLookup[]): IGroupTimestampEvent[] => {
    const timestampCountsMap = getTimestampCountsMap(events);
    const timestampsCountWithCount = Object.entries(timestampCountsMap).map(([timestamp, count]) => ({
        timestamp: +timestamp,
        count: count,
    }));
    return timestampsCountWithCount.sort((prevElement, nextElement) => prevElement.timestamp - nextElement.timestamp);
};

const addStartPoint = (data: IGroupTimestampEvent[], minTimestamp: number): IGroupTimestampEvent[] => {
    const firstPoint = data[0];

    if (firstPoint.timestamp > minTimestamp) {
        data.unshift({
            timestamp: minTimestamp,
            count: 0,
        });
    }

    return data;
};

const addEndPoint = (data: IGroupTimestampEvent[], maxTimestamp: number): IGroupTimestampEvent[] => {
    const lastPoint = data[data.length - 1];

    if (lastPoint.timestamp < maxTimestamp) {
        data.push({
            timestamp: maxTimestamp,
            count: 0,
        });
    }

    return data;
};

export const getCacheTimestampsChartData = (
    events: ICacheLookup[],
    minTimestamp: number,
    maxTimestamp: number,
): IGroupTimestampEvent[] => {
    const groupedTimestampsEvents = groupTimestampsEvents(events);
    addStartPoint(groupedTimestampsEvents, minTimestamp);
    addEndPoint(groupedTimestampsEvents, maxTimestamp);
    return groupedTimestampsEvents;
};
