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
import { CartesianGrid, Scatter, ScatterChart, Tooltip, XAxis, YAxis } from "recharts";

import { formatTransactionDuration, toFormattedTime } from "helpers";
import type { ITransactionalEntryPoint } from "models";

import { TransactionalMethodExecutionStats } from "../TransactionalMethodExecutionStats";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single transactional data
     */
    transactional: ITransactionalEntryPoint;
}

export const TransactionalAccordionBody = ({ transactional }: IProps) => {
    const data = transactional.executions;

    return (
        <>
            <div>
                <ScatterChart data={data} responsive className={styles.MainWrapper}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="timestamp" tickFormatter={toFormattedTime} />
                    <YAxis width="auto" tickFormatter={formatTransactionDuration} />
                    <Tooltip
                        labelFormatter={toFormattedTime}
                        itemStyle={{ color: "green" }}
                        formatter={(value, name) => {
                            const valueAsNum = Number(value);

                            if (name == "timestamp") {
                                return [toFormattedTime(valueAsNum), "Timestamp"];
                            } else {
                                return [formatTransactionDuration(valueAsNum), "Duration"];
                            }
                        }}
                    />
                    <Scatter dataKey="durationMs" fill="#00AB55FF" line lineType="joint" />
                </ScatterChart>
                <TransactionalMethodExecutionStats stats={transactional.executionStats} />
            </div>
        </>
    );
};
