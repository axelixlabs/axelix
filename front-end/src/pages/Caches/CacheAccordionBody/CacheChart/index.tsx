/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            <text x={calculatedX} y={calculatedY} textAnchor="middle" dominantBaseline="middle" fill="#000">
                {percent}%
            </text>
        );
    };

    return (
        <ResponsiveContainer height={100}>
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
    );
};
