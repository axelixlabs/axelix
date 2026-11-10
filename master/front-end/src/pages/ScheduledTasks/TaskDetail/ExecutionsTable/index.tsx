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
import { Pagination } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { EmptyHandler } from "@/components";
import type { IScheduledTaskExecutionRecord } from "@/models";

import { ExecutionRow } from "./ExecutionRow";
import { ExecutionsHeader } from "./ExecutionsHeader";
import styles from "./styles.module.css";

interface IProps {
    executions: IScheduledTaskExecutionRecord[];
}

type TExecutionsFilter = "all" | "failed";

const PAGE_SIZE = 8;

export const ExecutionsTable = ({ executions }: IProps) => {
    const { t } = useTranslation();

    const [filter, setFilter] = useState<TExecutionsFilter>("all");
    const [page, setPage] = useState(1);

    // TODO: The filter and the table only cover the latest 30 executions, the default page of the history API
    const failedExecutions = executions.filter((execution) => !execution.success);
    const visibleExecutions = filter === "failed" ? failedExecutions : executions;
    const firstIndex = (page - 1) * PAGE_SIZE;
    const pageExecutions = visibleExecutions.slice(firstIndex, firstIndex + PAGE_SIZE);

    const handleFilterChange = (newFilter: TExecutionsFilter): void => {
        setFilter(newFilter);
        setPage(1);
    };

    return (
        <div className={styles.MainWrapper}>
            <ExecutionsHeader
                filter={filter}
                total={executions.length}
                failed={failedExecutions.length}
                onFilterChange={handleFilterChange}
            />

            <div className={styles.ColumnHeader}>
                <span>{t("ScheduledTasks.started")}</span>
                <span>{t("ScheduledTasks.instance")}</span>
                <span className={styles.Duration}>{t("ScheduledTasks.duration")}</span>
                <span>{t("ScheduledTasks.result")}</span>
            </div>

            <EmptyHandler isEmpty={visibleExecutions.length === 0}>
                {pageExecutions.map((execution) => (
                    <ExecutionRow key={`${execution.startedAt}-${execution.instanceId}`} execution={execution} />
                ))}
            </EmptyHandler>

            <div className={styles.Footer}>
                <span>
                    {visibleExecutions.length > 0
                        ? t("ScheduledTasks.executionsShown", {
                              from: firstIndex + 1,
                              to: firstIndex + pageExecutions.length,
                              total: visibleExecutions.length,
                          })
                        : t("ScheduledTasks.executionsShownEmpty")}
                </span>
                <Pagination
                    className={styles.Pagination}
                    size="small"
                    current={page}
                    pageSize={PAGE_SIZE}
                    total={visibleExecutions.length}
                    showSizeChanger={false}
                    hideOnSinglePage
                    onChange={setPage}
                />
            </div>
        </div>
    );
};
