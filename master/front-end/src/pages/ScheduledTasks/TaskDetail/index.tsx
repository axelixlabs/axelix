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
import { Button, Popconfirm, Switch } from "antd";
import { useTranslation } from "react-i18next";

import { formatRelative } from "../mock";
import shared from "../shared.module.css";
import type { IScheduledTask, ITriggerOverride } from "../taskModel";
import { successRateClass, triggerTypeClass } from "../taskStyles";

import { DurationChart } from "./DurationChart";
import { EditTriggerPopover } from "./EditTriggerPopover";
import { EmptyExecutionHistory } from "./EmptyExecutionHistory";
import { ExecutionsTable } from "./ExecutionsTable";
import { FailuresByType } from "./FailuresByType";
import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
    onToggleEnabled: (enabled: boolean) => void;
    onRunNow: () => void;
    onEditTrigger: (override: ITriggerOverride) => void;
    onlyFailed: boolean;
    onOnlyFailedChange: (value: boolean) => void;
    page: number;
    onPageChange: (page: number) => void;
}

interface IStat {
    key: string;
    label: string;
    value: string;
    valueClass?: string;
}

export const TaskDetail = ({
    task,
    onToggleEnabled,
    onRunNow,
    onEditTrigger,
    onlyFailed,
    onOnlyFailedChange,
    page,
    onPageChange,
}: IProps) => {
    const { t } = useTranslation();

    const lastRunLabel = task.manualRuns.length
        ? t("ScheduledTasks.justNow")
        : task.lastRun
          ? formatRelative(task.lastRun.agoSeconds)
          : "—";

    const stats: IStat[] = task.hasHistory
        ? [
              { key: "lastRun", label: t("ScheduledTasks.statLastRun"), value: lastRunLabel },
              {
                  key: "success",
                  label: t("ScheduledTasks.statSuccess"),
                  value: `${task.successRate}%`,
                  valueClass: successRateClass(shared, task),
              },
              {
                  key: "failed",
                  label: t("ScheduledTasks.statFailed"),
                  value: String(task.failedCount),
                  valueClass: task.failedCount > 0 ? shared.RateBad : undefined,
              },
              { key: "avg", label: t("ScheduledTasks.statAvg"), value: task.avgLabel },
              { key: "p95", label: t("ScheduledTasks.statP95"), value: task.p95Label },
          ]
        : [
              { key: "lastRun", label: t("ScheduledTasks.statLastRun") },
              { key: "success", label: t("ScheduledTasks.statSuccess") },
              { key: "failed", label: t("ScheduledTasks.statFailed") },
              { key: "avg", label: t("ScheduledTasks.statAvg") },
              { key: "p95", label: t("ScheduledTasks.statP95") },
          ].map((stat) => ({ ...stat, value: "—", valueClass: styles.StatValueMuted }));

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Header}>
                <div className={styles.TitleBlock}>
                    <span className={`${shared.Mono} ${styles.Method}`}>{task.method}</span>
                    <span className={`${shared.Mono} ${styles.ClassName}`}>{task.className}</span>
                    <span className={styles.TriggerLine}>
                        <span className={`${shared.TypeBadge} ${triggerTypeClass(shared, task.type)}`}>
                            {task.type}
                        </span>
                        {task.human}
                        {task.showRaw && <span className={`${shared.Mono} ${styles.Raw}`}>{task.raw}</span>}
                    </span>
                </div>

                <div className={styles.Controls}>
                    {task.enabled ? (
                        <Popconfirm
                            title={t("ScheduledTasks.pauseTitle")}
                            description={t("ScheduledTasks.pauseDescription")}
                            okText={t("ScheduledTasks.pauseConfirm")}
                            cancelText={t("cancel")}
                            placement="bottomRight"
                            okButtonProps={{
                                style: {
                                    backgroundColor: "var(--prop-text)",
                                    borderColor: "var(--prop-text)",
                                },
                            }}
                            onConfirm={() => onToggleEnabled(false)}
                        >
                            <Switch
                                checked
                                checkedChildren={t("on")}
                                unCheckedChildren={t("off")}
                                onChange={() => undefined}
                            />
                        </Popconfirm>
                    ) : (
                        <Switch
                            checked={false}
                            checkedChildren={t("on")}
                            unCheckedChildren={t("off")}
                            onChange={() => onToggleEnabled(true)}
                        />
                    )}

                    <EditTriggerPopover task={task} onApply={onEditTrigger} />

                    <Button type="primary" loading={task.running} onClick={onRunNow}>
                        {task.running ? t("ScheduledTasks.running") : `▶ ${t("ScheduledTasks.runNow")}`}
                    </Button>
                </div>
            </div>

            <div className={styles.Stats}>
                {stats.map((stat) => (
                    <div className={styles.Stat} key={stat.key}>
                        <span className={styles.StatLabel}>{stat.label}</span>
                        <span className={`${shared.Mono} ${styles.StatValue} ${stat.valueClass ?? ""}`}>
                            {stat.value}
                        </span>
                    </div>
                ))}
            </div>

            {task.hasHistory ? (
                <>
                    <DurationChart runs={task.runs} maxDurationMs={task.maxDurationMs} />
                    <FailuresByType runs={task.runs} />
                    <ExecutionsTable
                        task={task}
                        onlyFailed={onlyFailed}
                        onOnlyFailedChange={onOnlyFailedChange}
                        page={page}
                        onPageChange={onPageChange}
                    />
                </>
            ) : (
                <EmptyExecutionHistory />
            )}
        </div>
    );
};
