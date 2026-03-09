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
import { useTranslation } from "react-i18next";
import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import { cacheHitsMissesChartToFormattedTime, getCacheTimestampsChartData, getTimelineInterval } from "helpers";
import type { IGetSingleCacheResponseBody } from "models";
import { CACHE_CHART_DOTS_VISIBILITY_THRESHOLD } from "utils";

import { CacheChartStats } from "../../CacheChartStats";

interface IProps {
    /**
     * Single cache data
     */
    singleCacheData: IGetSingleCacheResponseBody;
}

export const CacheChart = ({ singleCacheData }: IProps) => {
    const { t } = useTranslation();

    const interval = getTimelineInterval(singleCacheData);
    const { hits, misses } = singleCacheData;

    const allTimestamps = hits.concat(misses).map(({ timestamp }) => timestamp);

    const minTimestamp = Math.min(...allTimestamps);
    const maxTimestamp = Math.max(...allTimestamps);

    const hitsData = getCacheTimestampsChartData(hits, minTimestamp, maxTimestamp);
    const missesData = getCacheTimestampsChartData(misses, minTimestamp, maxTimestamp);

    const isHitsDotsNeeded = hitsData.length < CACHE_CHART_DOTS_VISIBILITY_THRESHOLD;
    const isMissesDotsNeeded = missesData.length < CACHE_CHART_DOTS_VISIBILITY_THRESHOLD;

    return (
        <>
            <ResponsiveContainer width="100%" height={330}>
                <LineChart>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />

                    <XAxis
                        dataKey="timestamp"
                        type="number"
                        domain={[minTimestamp, maxTimestamp]}
                        tickFormatter={(timestamp: number) => cacheHitsMissesChartToFormattedTime(timestamp, interval)}
                        interval="preserveStartEnd"
                    />

                    <YAxis allowDecimals={false} width="auto" />

                    <Line
                        data={hitsData}
                        type="monotone"
                        dataKey="count"
                        name={t("Caches.hits")}
                        stroke="#95de64"
                        strokeWidth={3}
                        dot={isHitsDotsNeeded}
                        activeDot={isHitsDotsNeeded}
                    />

                    <Line
                        data={missesData}
                        type="monotone"
                        dataKey="count"
                        name={t("Caches.misses")}
                        stroke="#69c0ff"
                        strokeWidth={3}
                        dot={isMissesDotsNeeded}
                        activeDot={isMissesDotsNeeded}
                    />

                    <Tooltip
                        labelFormatter={(timestamp: number) => cacheHitsMissesChartToFormattedTime(timestamp, interval)}
                    />
                    <Legend verticalAlign="top" align="right" />
                </LineChart>
            </ResponsiveContainer>
            <CacheChartStats singleCacheData={singleCacheData} />
        </>
    );
};
