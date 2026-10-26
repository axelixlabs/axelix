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

export const LibrariesHeader = () => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                <span />
                <span>{t("Dashboard.Dependencies.columns.library")}</span>
                <span>{t("Dashboard.Dependencies.columns.status")}</span>
                <span>{t("Dashboard.Dependencies.columns.succession")}</span>
                <span>{t("Dashboard.Dependencies.columns.category")}</span>
                <span className={styles.AlignEnd}>{t("Dashboard.Dependencies.columns.applications")}</span>
                <span>{t("Dashboard.Dependencies.share")}</span>
            </div>
        </>
    );
};
