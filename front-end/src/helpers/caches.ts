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
    ELookupOutcome,
    type ICacheChartDataPoint,
    type ICacheData,
    type ICacheLookup,
    type ICachesManager,
    type ITimelineData,
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

export const getTimelineInterval = (data: ICacheLookup[]): ITimelineData => {
    // That theoretically should not happen at all, since this function should be invoked
    // when we have at least 1 hit or miss, but still a safeguard
    if (data.length === 0) {
        return { maxTimestamp: -1, interval: SINGLE_CACHE_CHART_TIMELINE_STEP_S, minTimestamp: -1 };
    }

    const timelineData = {
        maxTimestamp: data.at(-1)!.timestamp,
        minTimestamp: data.at(0)!.timestamp,
    };

    const range = timelineData.maxTimestamp - timelineData.minTimestamp;

    if (range <= SINGLE_CACHE_CHART_TIMELINE_STEP_M) {
        return { interval: SINGLE_CACHE_CHART_TIMELINE_STEP_S, ...timelineData };
    }

    if (range <= 10 * SINGLE_CACHE_CHART_TIMELINE_STEP_M) {
        return { interval: SINGLE_CACHE_CHART_TIMELINE_STEP_5S, ...timelineData };
    }

    if (range <= SINGLE_CACHE_CHART_TIMELINE_STEP_H) {
        return { interval: SINGLE_CACHE_CHART_TIMELINE_STEP_M, ...timelineData };
    }

    if (range <= 6 * SINGLE_CACHE_CHART_TIMELINE_STEP_H) {
        return { interval: SINGLE_CACHE_CHART_TIMELINE_STEP_5M, ...timelineData };
    }

    if (range <= SINGLE_CACHE_CHART_TIMELINE_STEP_D) {
        return { interval: SINGLE_CACHE_CHART_TIMELINE_STEP_15M, ...timelineData };
    }

    if (range <= 7 * SINGLE_CACHE_CHART_TIMELINE_STEP_D) {
        return { interval: SINGLE_CACHE_CHART_TIMELINE_STEP_H, ...timelineData };
    }

    if (range <= 30 * SINGLE_CACHE_CHART_TIMELINE_STEP_D) {
        return { interval: SINGLE_CACHE_CHART_TIMELINE_STEP_D, ...timelineData };
    }

    return { interval: SINGLE_CACHE_CHART_TIMELINE_STEP_30D, ...timelineData };
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

/**
 * Builds the array of data points (basically, each data point is a Y coordinate value
 * on the cartesian plane) from the provided history of lookups.
 *
 * The data points (Y coordinate values) represent the ration of hits to total lookups into
 * the cache accumulated within the provided {@code slidingWindow}.
 *
 * @param lookupHistory the history of lookups from which the Y plane is built.
 * @param slidingWindow the sliding window size.
 *
 * @return Y coordinate values.
 */
export const buildChartData = (lookupHistory: ICacheLookup[], slidingWindow: number): ICacheChartDataPoint[] => {
    let windowHits = 0;
    const data: ICacheChartDataPoint[] = [];

    for (let i = 0; i < lookupHistory.length; i++) {
        const lookup = lookupHistory[i];

        if (i < slidingWindow) {
            // eslint-disable-next-line max-depth
            if (lookup.outcome === ELookupOutcome.HIT) {
                windowHits++;
            }
            data.push({ timestamp: lookup.timestamp, count: windowHits / (i + 1) });
        } else {
            // eslint-disable-next-line max-depth
            if (lookup.outcome == ELookupOutcome.HIT) {
                windowHits++;
            }

            // lookupHistory[i - windowSize] is a tail of sliding window
            // we know that 'i' is at least has 'windowSize'
            // eslint-disable-next-line max-depth
            if (lookupHistory[i - slidingWindow].outcome === ELookupOutcome.HIT) {
                windowHits--;
            }

            data.push({ timestamp: lookup.timestamp, count: windowHits / slidingWindow });
        }
    }

    return data;
};
