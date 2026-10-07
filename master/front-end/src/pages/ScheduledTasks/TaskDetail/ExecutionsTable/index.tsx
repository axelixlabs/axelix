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
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import type { IScheduledTask } from "@/models";
import { PAGE_SIZE } from "@/utils";

import { ExecutionsTableHeader } from "./ExecutionsTableHeader";
import { ExecutionsTableRow } from "./ExecutionsTableRow";
import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
    onlyFailed: boolean;
    setOnlyFailed: Dispatch<SetStateAction<boolean>>;
    page: number;
    setPage: Dispatch<SetStateAction<number>>;
}

export const ExecutionsTable = ({ task, onlyFailed, setOnlyFailed, page, setPage }: IProps) => {
    const { t } = useTranslation();

    const list = [...task.manualRuns, ...[...task.runs].reverse()].filter((run) => !onlyFailed || !run.succeeded);

    const pageCount = Math.max(1, Math.ceil(list.length / PAGE_SIZE));
    const currentPage = Math.min(page, pageCount - 1);
    const pageItems = list.slice(currentPage * PAGE_SIZE, currentPage * PAGE_SIZE + PAGE_SIZE);

    const shownLabel = list.length
        ? t("ScheduledTasks.executionsShown", {
              from: currentPage * PAGE_SIZE + 1,
              to: Math.min(list.length, currentPage * PAGE_SIZE + PAGE_SIZE),
              total: list.length,
          })
        : t("ScheduledTasks.executionsShownEmpty");

    return (
        <>
            <div className={styles.MainWrapper}>
                <ExecutionsTableHeader task={task} setOnlyFailed={setOnlyFailed} onlyFailed={onlyFailed} />

                <div className={`TextUltraSmall ${styles.ColumnHeader}`}>
                    <div>{t("ScheduledTasks.started")}</div>
                    <div>{t("ScheduledTasks.instance")}</div>
                    <div className={styles.ColumnHeaderDuration}>{t("ScheduledTasks.duration")}</div>
                    <div>{t("ScheduledTasks.result")}</div>
                </div>

                {pageItems.map((run, index) => (
                    <ExecutionsTableRow run={run} key={`${currentPage}-${index}`} />
                ))}

                <div className={`TextUltraSmall ${styles.Footer}`}>
                    <span className={styles.Shown}>{shownLabel}</span>

                    <Pagination
                        size="small"
                        current={currentPage + 1}
                        pageSize={PAGE_SIZE}
                        total={list.length}
                        showSizeChanger={false}
                        onChange={(nextPage) => setPage(nextPage - 1)}
                    />
                </div>
            </div>
        </>
    );
};
