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
import { useTranslation } from "react-i18next";

import { type IScheduledTaskExecution, NOW_SECONDS, formatDuration, formatTimeOfDay } from "../../mock";
import shared from "../../shared.module.css";
import type { IScheduledTask } from "../../taskModel";

import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
    onlyFailed: boolean;
    onOnlyFailedChange: (value: boolean) => void;
    page: number;
    onPageChange: (page: number) => void;
}

const PAGE_SIZE = 8;

export const ExecutionsTable = ({ task, onlyFailed, onOnlyFailedChange, page, onPageChange }: IProps) => {
    const { t } = useTranslation();

    const list = [...task.manualRuns, ...[...task.runs].reverse()].filter((run) => !onlyFailed || !run.succeeded);

    const pageCount = Math.max(1, Math.ceil(list.length / PAGE_SIZE));
    const currentPage = Math.min(page, pageCount - 1);
    const pageItems = list.slice(currentPage * PAGE_SIZE, currentPage * PAGE_SIZE + PAGE_SIZE);

    const goTo = (target: number): void => onPageChange(Math.max(0, Math.min(pageCount - 1, target)));

    const shownLabel = list.length
        ? t("ScheduledTasks.executionsShown", {
              from: currentPage * PAGE_SIZE + 1,
              to: Math.min(list.length, currentPage * PAGE_SIZE + PAGE_SIZE),
              total: list.length,
          })
        : t("ScheduledTasks.executionsShownEmpty");

    const renderResult = (run: IScheduledTaskExecution) => (
        <span className={styles.Result}>
            <span className={styles.ResultLine}>
                {run.succeeded ? (
                    <span className={styles.ResultSucceeded}>✓ {t("ScheduledTasks.succeeded")}</span>
                ) : (
                    <span className={`${shared.Mono} ${styles.ResultFailed}`}>✕ {run.errorType}</span>
                )}
                {run.manual && <span className={styles.ManualChip}>{t("ScheduledTasks.manual")}</span>}
            </span>
            {run.errorMessage && <span className={styles.ResultMessage}>{run.errorMessage}</span>}
        </span>
    );

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Header}>
                <span className={styles.Title}>{t("ScheduledTasks.executions")}</span>

                <div className={styles.Filter}>
                    <button
                        type="button"
                        onClick={() => onOnlyFailedChange(false)}
                        className={`${styles.FilterOption} ${onlyFailed ? "" : styles.FilterActive}`}
                    >
                        {t("ScheduledTasks.all")} {task.runs.length}
                    </button>
                    <button
                        type="button"
                        onClick={() => onOnlyFailedChange(true)}
                        className={`${styles.FilterOption} ${styles.FilterFailed} ${
                            onlyFailed ? styles.FilterFailedActive : ""
                        } ${onlyFailed ? styles.FilterActive : ""}`}
                    >
                        {t("ScheduledTasks.failed")} {task.failedCount}
                    </button>
                </div>
            </div>

            <div className={styles.ColumnHeader}>
                <span>{t("ScheduledTasks.started")}</span>
                <span>{t("ScheduledTasks.instance")}</span>
                <span className={styles.ColumnHeaderDuration}>{t("ScheduledTasks.duration")}</span>
                <span>{t("ScheduledTasks.result")}</span>
            </div>

            {pageItems.map((run, index) => (
                <div
                    key={`${currentPage}-${index}`}
                    className={`${styles.Row} ${run.succeeded ? "" : styles.RowFailed}`}
                >
                    <span className={`${shared.Mono} ${styles.StartedTime}`}>
                        {formatTimeOfDay(NOW_SECONDS - run.agoSeconds)}
                    </span>
                    <span className={`${shared.Mono} ${styles.Instance}`}>{run.instance}</span>
                    <span className={`${shared.Mono} ${styles.Duration}`}>{formatDuration(run.durationMs)}</span>
                    {renderResult(run)}
                </div>
            ))}

            <div className={styles.Footer}>
                <span className={styles.Shown}>{shownLabel}</span>
                <div className={styles.Pager}>
                    <button
                        type="button"
                        className={styles.PagerButton}
                        disabled={currentPage === 0}
                        onClick={() => goTo(currentPage - 1)}
                    >
                        ‹
                    </button>
                    {Array.from({ length: pageCount }, (_, index) => (
                        <button
                            type="button"
                            key={index}
                            onClick={() => goTo(index)}
                            className={`${styles.PagerButton} ${index === currentPage ? styles.PageActive : ""}`}
                        >
                            {index + 1}
                        </button>
                    ))}
                    <button
                        type="button"
                        className={styles.PagerButton}
                        disabled={currentPage === pageCount - 1}
                        onClick={() => goTo(currentPage + 1)}
                    >
                        ›
                    </button>
                </div>
            </div>
        </div>
    );
};
