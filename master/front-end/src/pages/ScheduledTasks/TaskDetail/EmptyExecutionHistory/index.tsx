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

import styles from "./styles.module.css";

/**
 * The 11a "keep the layout, explain in place" empty state: shown in place of the chart, failures
 * and executions log when a task has no recorded executions.
 */
export const EmptyExecutionHistory = () => {
    const { t } = useTranslation();

    return (
        <div className={styles.MainWrapper}>
            <span className={styles.Icon}>◷</span>
            <span className={styles.Title}>{t("ScheduledTasks.noHistoryTitle")}</span>
            <span className={styles.Description}>{t("ScheduledTasks.noHistoryDescription")}</span>
            <a className={styles.DocsLink} target="_blank" rel="noopener noreferrer" href="https://axelix.io/docs">
                {t("ScheduledTasks.readDocs")}
            </a>
        </div>
    );
};
