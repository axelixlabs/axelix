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
import { App } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { TaskDetail } from "./TaskDetail";
import { TaskList } from "./TaskList";
import { type IScheduledTaskExecution, createManualRun, formatDuration } from "./mock";
import styles from "./styles.module.css";
import { type ITriggerOverride, SEED_ORDER, buildTask, groupTasksByType } from "./taskModel";

/*
 * The redesigned Scheduled Tasks page (design 9b "task list + task detail", with the 11a empty
 * state for tasks without execution history). Everything here is driven by mock data - the backend
 * API that will supply real execution history does not exist yet, so all interactions (toggle,
 * edit trigger, run now, filter, pagination) operate on local state only.
 */

const RUN_DURATION_MS = 1600;

const ScheduledTasks = () => {
    const { t } = useTranslation();
    const { message } = App.useApp();

    const [selectedKey, setSelectedKey] = useState("processPendingPayments");
    const [search, setSearch] = useState("");
    const [disabled, setDisabled] = useState<Record<string, boolean>>({ refreshSpecialtiesCache: true });
    const [triggers, setTriggers] = useState<Record<string, ITriggerOverride>>({});
    const [manualRuns, setManualRuns] = useState<Record<string, IScheduledTaskExecution[]>>({});
    const [running, setRunning] = useState<Record<string, boolean>>({});
    const [onlyFailed, setOnlyFailed] = useState(false);
    const [page, setPage] = useState(0);

    const tasks = SEED_ORDER.map((seed) =>
        buildTask(seed, {
            enabled: !disabled[seed.key],
            triggerOverride: triggers[seed.key],
            manualRuns: manualRuns[seed.key] ?? [],
            running: Boolean(running[seed.key]),
        }),
    );

    const selectedTask = tasks.find((task) => task.key === selectedKey) ?? tasks[0];

    const query = search.trim().toLowerCase();
    const visibleTasks = query
        ? tasks.filter(
              (task) => task.method.toLowerCase().includes(query) || task.className.toLowerCase().includes(query),
          )
        : tasks;

    const selectTask = (key: string): void => {
        setSelectedKey(key);
        setOnlyFailed(false);
        setPage(0);
    };

    const toggleEnabled = (enabled: boolean): void => {
        setDisabled((previous) => ({ ...previous, [selectedKey]: !enabled }));
        message.success(
            enabled
                ? t("ScheduledTasks.resumedToast", { schedule: selectedTask.human })
                : t("ScheduledTasks.pausedToast"),
        );
    };

    const editTrigger = (override: ITriggerOverride): void => {
        setTriggers((previous) => ({ ...previous, [selectedKey]: override }));
        message.success(t("ScheduledTasks.triggerUpdatedToast", { schedule: override.human }));
    };

    const runNow = (): void => {
        if (running[selectedKey]) {
            return;
        }

        const { key, seed } = selectedTask;
        setRunning((previous) => ({ ...previous, [key]: true }));

        setTimeout(() => {
            const run = createManualRun(seed);
            setRunning((previous) => ({ ...previous, [key]: false }));
            setManualRuns((previous) => ({ ...previous, [key]: [run, ...(previous[key] ?? [])] }));
            setOnlyFailed(false);
            setPage(0);
            message.success(t("ScheduledTasks.runFinishedToast", { duration: formatDuration(run.durationMs) }));
        }, RUN_DURATION_MS);
    };

    return (
        <div className={styles.Layout}>
            <TaskList
                groups={groupTasksByType(visibleTasks)}
                selectedKey={selectedTask.key}
                onSelect={selectTask}
                search={search}
                onSearch={setSearch}
            />

            <TaskDetail
                key={selectedTask.key}
                task={selectedTask}
                onToggleEnabled={toggleEnabled}
                onRunNow={runNow}
                onEditTrigger={editTrigger}
                onlyFailed={onlyFailed}
                onOnlyFailedChange={setOnlyFailed}
                page={page}
                onPageChange={setPage}
            />
        </div>
    );
};

export default ScheduledTasks;
