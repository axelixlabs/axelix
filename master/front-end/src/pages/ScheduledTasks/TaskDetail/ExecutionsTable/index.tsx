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
import dayjs from "dayjs";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { EmptyHandler } from "@/components";
import { formatTransactionDuration } from "@/helpers";
import type { IScheduledTaskExecutionRecord } from "@/models";

import shared from "../../shared.module.css";

import styles from "./styles.module.css";

interface IProps {
    executions: IScheduledTaskExecutionRecord[];
}

type TExecutionsFilter = "all" | "failed";

const PAGE_SIZE = 8;

const formatStartedAt = (startedAt: string): string => {
    const started = dayjs(startedAt);

    return started.format(started.isSame(dayjs(), "day") ? "HH:mm:ss" : "DD.MM HH:mm:ss");
};

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
            <div className={styles.Header}>
                <span className={styles.Title}>{t("ScheduledTasks.executions")}</span>

                <div className={styles.Filter}>
                    <button
                        type="button"
                        onClick={() => handleFilterChange("all")}
                        className={`${styles.FilterOption} ${filter === "all" ? styles.FilterActive : ""}`}
                    >
                        {t("ScheduledTasks.all")} {executions.length}
                    </button>
                    <button
                        type="button"
                        onClick={() => handleFilterChange("failed")}
                        className={`${styles.FilterOption} ${styles.FilterFailed} ${filter === "failed" ? styles.FilterActive : ""}`}
                    >
                        {t("ScheduledTasks.failed")} {failedExecutions.length}
                    </button>
                </div>
            </div>

            <div className={styles.ColumnHeader}>
                <span>{t("ScheduledTasks.started")}</span>
                <span>{t("ScheduledTasks.instance")}</span>
                <span className={styles.Duration}>{t("ScheduledTasks.duration")}</span>
                <span>{t("ScheduledTasks.result")}</span>
            </div>

            <EmptyHandler isEmpty={visibleExecutions.length === 0}>
                {pageExecutions.map((execution) => (
                    <div
                        key={`${execution.startedAt}-${execution.instanceId}`}
                        className={`${styles.Row} ${execution.success ? "" : styles.RowFailed}`}
                    >
                        <span className={shared.Mono}>{formatStartedAt(execution.startedAt)}</span>
                        <span className={`${shared.Mono} ${styles.Instance}`}>{execution.instanceName}</span>
                        <span className={`${shared.Mono} ${styles.Duration}`}>
                            {formatTransactionDuration(execution.durationMillis)}
                        </span>
                        {execution.success ? (
                            <span className={styles.ResultSucceeded}>✓ {t("ScheduledTasks.succeeded")}</span>
                        ) : (
                            <span className={styles.Result}>
                                <span className={`${shared.Mono} ${styles.ResultFailed}`}>
                                    ✕ {execution.errorType ?? t("ScheduledTasks.failed")}
                                </span>
                                {execution.errorMessage && (
                                    <span className={styles.ResultMessage}>{execution.errorMessage}</span>
                                )}
                            </span>
                        )}
                    </div>
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
