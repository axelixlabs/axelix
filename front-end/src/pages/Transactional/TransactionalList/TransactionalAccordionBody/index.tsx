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
import { type JSX, useState } from "react";
import { CartesianGrid, Scatter, ScatterChart, type ScatterPointItem, Tooltip, XAxis, YAxis } from "recharts";
import type { ScatterShapeProps } from "recharts";

import { formatTransactionDuration, toFormattedTime, toFormattedTimeWithMs } from "helpers";
import type { IExecutionWithDurationMs, IQueryData, ITransactionalEntryPoint } from "models";

import { TransactionalMethodExecutionStats } from "../TransactionalMethodExecutionStats";

import { ExecutionDetails } from "./ExecutionDetails";
import styles from "./styles.module.css";

interface IProps {
    /**
     * Single transactional data
     */
    transactional: ITransactionalEntryPoint;
}

export const TransactionalAccordionBody = ({ transactional }: IProps) => {
    const [selectedExecution, setSelectedExecution] = useState<IExecutionWithDurationMs | null>(null);

    const data: IExecutionWithDurationMs[] = transactional.executions.map((execution) => ({
        ...execution,
        durationMs: execution.endTimestampMs - execution.startTimestampMs,
    }));

    const handleShapeClick = (data: ScatterPointItem & IExecutionWithDurationMs): void => {
        setSelectedExecution(data);
    };

    const renderShape = (props: ScatterShapeProps): JSX.Element => {
        const data = props as ScatterShapeProps & IQueryData;
        const isSelected = selectedExecution?.startTimestampMs === data?.startTimestampMs;

        return (
            <circle
                cx={props.cx}
                cy={props.cy}
                r={5}
                className={`${styles.ScatterShape} ${isSelected ? styles.ActiveScatterShape : ""}`}
            />
        );
    };

    return (
        <>
            <div>
                <ScatterChart data={data} responsive className={styles.ScatterChart}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="startTimestampMs" tickFormatter={toFormattedTime} />
                    <YAxis dataKey="durationMs" width="auto" tickFormatter={formatTransactionDuration} />
                    <Tooltip
                        formatter={(value, name) => {
                            const valueAsNum = Number(value);

                            if (name === "startTimestampMs") {
                                return [toFormattedTimeWithMs(valueAsNum), "Timestamp"];
                            } else {
                                return [formatTransactionDuration(valueAsNum), "Duration"];
                            }
                        }}
                    />
                    <Scatter
                        dataKey="durationMs"
                        fill="#00ab55"
                        line
                        lineType="joint"
                        onClick={handleShapeClick}
                        shape={renderShape}
                    />
                </ScatterChart>
                <TransactionalMethodExecutionStats stats={transactional.executionStats} />
            </div>
            <ExecutionDetails selectedExecution={selectedExecution} setSelectedExecution={setSelectedExecution} />
        </>
    );
};
