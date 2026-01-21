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
import { Bar, BarChart, CartesianGrid, Tooltip, XAxis, YAxis } from "recharts";

import { formatExecutionTimestamp } from "helpers";
import type { ITransactionalResponseData } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single transactional data
     */
    transactional: ITransactionalResponseData;
}

export const TransactionalAccordionBody = ({ transactional }: IProps) => {
    const data = transactional.executions;

    return (
        <BarChart data={data} responsive className={styles.MainWrapper}>
            <defs>
                <linearGradient id="gradient" x1="0" y1="300" x2="0" y2="0" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#73BF69" />
                    <stop offset="50%" stopColor="#FF9830" />
                    <stop offset="90%" stopColor="#F2495C" />
                </linearGradient>
            </defs>

            <CartesianGrid strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="timestamp" tickFormatter={formatExecutionTimestamp} />
            <YAxis width="auto" />

            {/* TODO: When we decide how to display the time, we will add it to the tooltip */}
            <Tooltip />
            <Bar dataKey="durationsMs" fill="url(#gradient)" />
        </BarChart>
    );
};
