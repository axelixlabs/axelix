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

import type { IDetailStat, IScheduledTask } from "@/models";

import { formatRelative } from "../../mock";
import sharedStyles from "../../shared.module.css";
import { successRateClass } from "../../taskStyles";

import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
}

export const TaskDetailStats = ({ task }: IProps) => {
    const { t } = useTranslation();

    const { lastRun, manualRuns, hasHistory, failedCount, successRate, avgLabel, p95Label } = task;

    const getLastRunLabel = (): string => {
        if (manualRuns.length) {
            return t("ScheduledTasks.justNow");
        }

        if (lastRun) {
            return formatRelative(lastRun.agoSeconds);
        }

        return "—";
    };

    const lastRunLabel = getLastRunLabel();

    const stats: IDetailStat[] = hasHistory
        ? [
              { key: "lastRun", label: t("ScheduledTasks.statLastRun"), value: lastRunLabel },
              {
                  key: "success",
                  label: t("ScheduledTasks.statSuccess"),
                  value: `${successRate}%`,
                  valueClass: successRateClass(sharedStyles, task),
              },
              {
                  key: "failed",
                  label: t("ScheduledTasks.statFailed"),
                  value: String(failedCount),
                  valueClass: failedCount > 0 ? sharedStyles.RateBad : undefined,
              },
              { key: "avg", label: t("ScheduledTasks.statAvg"), value: avgLabel },
              { key: "p95", label: t("ScheduledTasks.statP95"), value: p95Label },
          ]
        : [
              { key: "lastRun", label: t("ScheduledTasks.statLastRun") },
              { key: "success", label: t("ScheduledTasks.statSuccess") },
              { key: "failed", label: t("ScheduledTasks.statFailed") },
              { key: "avg", label: t("ScheduledTasks.statAvg") },
              { key: "p95", label: t("ScheduledTasks.statP95") },
          ].map((stat) => ({ ...stat, value: "—", valueClass: styles.StatValueMuted }));

    return (
        <>
            <div className={styles.MainWrapper}>
                {stats.map(({ label, value, valueClass, key }) => (
                    <div className={styles.Stat} key={key}>
                        <div className={`TextUltraSmall ${styles.StatLabel}`}>{label}</div>
                        <div className={`TextMedium ${sharedStyles.Mono} ${styles.StatValue} ${valueClass ?? ""}`}>
                            {value}
                        </div>
                    </div>
                ))}
            </div>
        </>
    );
};
