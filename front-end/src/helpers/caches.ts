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
import type { ICachesManager, IGetSingleCacheResponseBody, ISingleCacheChartEntity } from "models";

import { SINGLE_CACHE_CHART_TIMELINE_STEP } from "../utils";

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

const floorTimestamp = (timestamp: number, interval: number): number => {
    return Math.floor(timestamp / interval) * interval;
};

export const createHitsAndMissesGroup = (data: IGetSingleCacheResponseBody): ISingleCacheChartEntity[] => {
    const groupHitsAndMisses: Record<number, ISingleCacheChartEntity> = {};

    for (const { timestamp } of data.hits) {
        if (!groupHitsAndMisses[timestamp]) {
            groupHitsAndMisses[timestamp] = {
                timestamp: timestamp,
                hits: 0,
                misses: 0,
            };
        }
        groupHitsAndMisses[timestamp].hits++;
    }

    for (const { timestamp } of data.misses) {
        if (!groupHitsAndMisses[timestamp]) {
            groupHitsAndMisses[timestamp] = {
                timestamp: timestamp,
                hits: 0,
                misses: 0,
            };
        }
        groupHitsAndMisses[timestamp].misses++;
    }

    return Object.values(groupHitsAndMisses);
};

const normalizeChartData = (data: ISingleCacheChartEntity[]): ISingleCacheChartEntity[] => {
    const groupedData: Record<number, ISingleCacheChartEntity> = {};

    for (const item of data) {
        const normalizedData = floorTimestamp(item.timestamp, SINGLE_CACHE_CHART_TIMELINE_STEP);

        if (!groupedData[normalizedData]) {
            groupedData[normalizedData] = {
                timestamp: normalizedData,
                hits: 0,
                misses: 0,
            };
        }

        groupedData[normalizedData].hits += item.hits;
        groupedData[normalizedData].misses += item.misses;
    }

    return Object.values(groupedData).sort((a, b) => a.timestamp - b.timestamp);
};

const buildContinuousTimeline = (normalizedData: ISingleCacheChartEntity[]): ISingleCacheChartEntity[] => {
    if (!normalizedData.length) {
        return [];
    }

    const firstTimestamp = normalizedData[0].timestamp;
    const lastTimestamp = normalizedData[normalizedData.length - 1].timestamp;

    const timelineMap: Record<number, ISingleCacheChartEntity> = {};
    for (const item of normalizedData) {
        timelineMap[item.timestamp] = item;
    }

    const chartData: ISingleCacheChartEntity[] = [];

    for (let timestamp = firstTimestamp; timestamp <= lastTimestamp; timestamp += SINGLE_CACHE_CHART_TIMELINE_STEP) {
        const defaultChartData = {
            timestamp: timestamp,
            hits: 0,
            misses: 0,
        };
        chartData.push(timelineMap[timestamp] ?? defaultChartData);
    }

    return chartData;
};

export const getChartdata = (data: ISingleCacheChartEntity[]): ISingleCacheChartEntity[] => {
    const normalized = normalizeChartData(data);
    return buildContinuousTimeline(normalized);
};
