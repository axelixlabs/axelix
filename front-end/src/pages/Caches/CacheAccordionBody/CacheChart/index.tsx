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

import { buildChartData, cacheHitsMissesChartToFormattedTime, getTimelineInterval } from "helpers";
import type { IGetSingleCacheResponseBody } from "models";

import { CacheChartStats } from "../../CacheChartStats";

interface IProps {
    /**
     * Single cache data
     */
    singleCacheData: IGetSingleCacheResponseBody;
}

export const CacheChart = ({ singleCacheData }: IProps) => {
    const { t } = useTranslation();

    const { interval, minTimestamp, maxTimestamp } = getTimelineInterval(singleCacheData.lookupHistory);
    const { lookupHistory } = singleCacheData;

    const slidingRatio = buildChartData(lookupHistory, 50);

    return (
        <>
            <ResponsiveContainer width="100%" height={330}>
                <LineChart data={slidingRatio}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />

                    <XAxis
                        dataKey="timestamp"
                        type="number"
                        domain={[minTimestamp, maxTimestamp]}
                        tickFormatter={(timestamp: number) => cacheHitsMissesChartToFormattedTime(timestamp, interval)}
                        interval="preserveStartEnd"
                    />

                    <YAxis domain={[0, 1]} allowDecimals width="auto" />

                    <Line
                        type="monotone"
                        dataKey="count"
                        name={t("Caches.ratio")}
                        stroke="#95de64"
                        dot={false}
                        strokeWidth={3}
                    />

                    <Tooltip
                        labelFormatter={(timestamp: number) => cacheHitsMissesChartToFormattedTime(timestamp, interval)}
                    />
                    <Legend verticalAlign="top" align="right" />
                </LineChart>
            </ResponsiveContainer>
            <CacheChartStats cacheData={singleCacheData} />
        </>
    );
};
