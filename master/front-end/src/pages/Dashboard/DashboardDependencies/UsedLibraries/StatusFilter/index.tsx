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

import type { ESupportStatus, IDependenciesDashboardResponseBody } from "@/models";
import { SUPPORT_STATUS_ORDER, dependencyHealthColor, supportSignalLabelKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    librariesByStatus: IDependenciesDashboardResponseBody["librariesByStatus"];
    activeStatuses: ESupportStatus[];
    onToggle: (status: ESupportStatus) => void;
}

export const StatusFilter = ({ librariesByStatus, activeStatuses, onToggle }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                <span className={styles.Label}>{t("Dashboard.Dependencies.statusFilter")}</span>
                {SUPPORT_STATUS_ORDER.map((status) => (
                    <button
                        key={status}
                        type="button"
                        onClick={() => onToggle(status)}
                        className={`${styles.Chip} ${styles[status]} ${activeStatuses.includes(status) ? styles.Active : ""}`}
                    >
                        <span className={styles.Dot} style={{ backgroundColor: dependencyHealthColor[status] }} />
                        {t(supportSignalLabelKey[status])}
                        <span className={styles.Count}>{librariesByStatus[status] ?? 0}</span>
                    </button>
                ))}
            </div>
        </>
    );
};
