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
import { useEffect, useState } from "react";
import { useParams } from "react-router";

import { EmptyHandler, Loader } from "@/components";
import { fetchData } from "@/helpers";
import { type IScheduledTaskHistoryResponse, StatefulRequest, type TScheduledTaskItem } from "@/models";
import { getScheduledTaskHistory } from "@/services";

import { DurationChart } from "./DurationChart";
import { EmptyExecutionHistory } from "./EmptyExecutionHistory";
import { ExecutionsTable } from "./ExecutionsTable";
import { FailuresByType } from "./FailuresByType";
import { TaskHeader } from "./TaskHeader";
import { TaskStats } from "./TaskStats";
import styles from "./styles.module.css";

interface IProps {
    item: TScheduledTaskItem;
    onChange: () => Promise<void>;
}

export const TaskDetail = ({ item, onChange }: IProps) => {
    const { instanceId } = useParams();

    const [history, setHistory] = useState(StatefulRequest.loading<IScheduledTaskHistoryResponse>());

    // TODO: Refresh the history while the page is open, new executions only appear after reselecting the task
    useEffect(() => {
        fetchData(setHistory, () => getScheduledTaskHistory(instanceId!, item.task.taskId));
    }, []);

    const renderHistory = () => {
        if (history.loading) {
            return <Loader />;
        }

        if (history.error) {
            return <EmptyHandler isEmpty />;
        }

        const { summary, failuresByErrorType, executions } = history.response!;

        return (
            <>
                <TaskStats summary={summary} enabled={item.task.enabled} />

                {summary ? (
                    <>
                        <DurationChart executions={executions} />
                        <FailuresByType failures={failuresByErrorType} />
                        <ExecutionsTable executions={executions} />
                    </>
                ) : (
                    <EmptyExecutionHistory />
                )}
            </>
        );
    };

    return (
        <div
            className={`${styles.MainWrapper} ${history.response?.summary === null ? styles.MainWrapperStretched : ""}`}
        >
            <TaskHeader item={item} onChange={onChange} />

            {renderHistory()}
        </div>
    );
};
