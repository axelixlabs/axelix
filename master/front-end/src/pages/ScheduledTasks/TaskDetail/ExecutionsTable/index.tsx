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
import { useLayoutEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";

import { EmptyHandler } from "@/components";
import type { IScheduledTaskExecutionRecord, TScheduledTaskExecutionsFilter } from "@/models";

import { ExecutionRow } from "./ExecutionRow";
import { ExecutionsHeader } from "./ExecutionsHeader";
import styles from "./styles.module.css";

interface IProps {
    executions: IScheduledTaskExecutionRecord[];
}

const BOTTOM_SPACING = 24;
const sumHeights = (sum: number, height: number): number => sum + height;

export const ExecutionsTable = ({ executions }: IProps) => {
    const { t } = useTranslation();

    const [filter, setFilter] = useState<TScheduledTaskExecutionsFilter>("all");
    const [page, setPage] = useState(1);
    const [pageSize, setPageSize] = useState(1);
    const measurementRef = useRef<HTMLDivElement>(null);
    const footerRef = useRef<HTMLDivElement>(null);

    // TODO: The filter and the table only cover the latest 30 executions, the default page of the history API
    const failedExecutions = executions.filter((execution) => !execution.success);
    const visibleExecutions = filter === "failed" ? failedExecutions : executions;
    const currentPage = Math.min(page, Math.max(1, Math.ceil(visibleExecutions.length / pageSize)));
    const firstIndex = (currentPage - 1) * pageSize;
    const pageExecutions = visibleExecutions.slice(firstIndex, firstIndex + pageSize);

    useLayoutEffect(() => {
        const measurement = measurementRef.current!;
        const footer = footerRef.current!;
        const updatePageSize = (): void => {
            // Document coordinates keep pagination stable while scrolling.
            const availableHeight =
                window.innerHeight -
                (measurement.getBoundingClientRect().top + window.scrollY) -
                Math.max(footer.getBoundingClientRect().height, 44) -
                BOTTOM_SPACING;
            const heights = Array.from(measurement.children, (row) => row.getBoundingClientRect().height);
            let nextPageSize = 1;
            // Choose a consistent size that fits every page, including wrapped errors.
            for (let size = 1; size <= heights.length; size++) {
                const fits = heights.every(
                    (_, start) =>
                        start % size !== 0 ||
                        heights.slice(start, start + size).reduce(sumHeights, 0) <= availableHeight,
                );
                if (fits) nextPageSize = size;
            }
            setPageSize(nextPageSize);
        };
        const observer = new ResizeObserver(updatePageSize);
        observer.observe(measurement.parentElement!.parentElement!.parentElement!);
        observer.observe(measurement);
        observer.observe(footer);
        window.addEventListener("resize", updatePageSize);
        updatePageSize();
        return () => {
            observer.disconnect();
            window.removeEventListener("resize", updatePageSize);
        };
    }, [executions, filter]);

    const handleFilterChange = (newFilter: TScheduledTaskExecutionsFilter): void => {
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

            <div className={styles.Rows}>
                <div ref={measurementRef} className={styles.Measurement} aria-hidden="true" inert>
                    {visibleExecutions.map((execution) => (
                        <ExecutionRow key={`${execution.startedAt}-${execution.instanceId}`} execution={execution} />
                    ))}
                </div>
                <EmptyHandler isEmpty={visibleExecutions.length === 0}>
                    {pageExecutions.map((execution) => (
                        <ExecutionRow key={`${execution.startedAt}-${execution.instanceId}`} execution={execution} />
                    ))}
                </EmptyHandler>
            </div>

            <div ref={footerRef} className={styles.Footer}>
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
                    current={currentPage}
                    pageSize={pageSize}
                    total={visibleExecutions.length}
                    showSizeChanger={false}
                    hideOnSinglePage
                    onChange={setPage}
                />
            </div>
        </div>
    );
};
