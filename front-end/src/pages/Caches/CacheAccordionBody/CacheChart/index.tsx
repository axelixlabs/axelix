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
import type { JSX } from "react";
import { useTranslation } from "react-i18next";
import {
    Bar,
    BarChart,
    CartesianGrid,
    Cell,
    type LabelProps,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from "recharts";

import type { ICacheData } from "models";

interface IProps {
    /**
     * Single cache data
     */
    cache: ICacheData;
}

export const CacheChart = ({ cache }: IProps) => {
    const { t } = useTranslation();

    const data = [
        {
            name: t("Caches.hitsCount"),
            value: cache.hitsCount,
        },
        {
            name: t("Caches.missesCount"),
            value: cache.missesCount,
        },
    ];

    const renderLabel = (props: LabelProps): JSX.Element => {
        const { x = 0, y = 0, width = 0, height = 0, value = 0 } = props;

        const totalCount = data.reduce((acc, item) => acc + item.value, 0);

        const percent = !totalCount ? 0 : Math.round((Number(value) / totalCount) * 100);

        const calculatedX = Number(x) + Number(width) + 30;
        const calculatedY = Number(y) + Number(height) / 2;

        return (
            <>
                <text x={calculatedX} y={calculatedY} textAnchor="middle" dominantBaseline="middle" fill="#000">
                    {percent}%
                </text>
            </>
        );
    };

    return (
        <>
            <ResponsiveContainer
                height={100}
                // Only inlined styles are permitted here
                style={{
                    marginTop: -8,
                }}
            >
                <BarChart data={data} layout="vertical" margin={{ left: 40, right: 90 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" />
                    <YAxis type="category" dataKey="name" />
                    <Tooltip />
                    <Bar dataKey="value" radius={[0, 6, 6, 0]} label={(props) => renderLabel(props)}>
                        {data.map(({ name }) => {
                            const fill = name === t("Caches.hitsCount") ? "#00ab55" : "#b37feb";

                            return <Cell key={name} fill={fill} />;
                        })}
                    </Bar>
                </BarChart>
            </ResponsiveContainer>
        </>
    );
};
