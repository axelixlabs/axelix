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

interface IProps {
    /**
     * The name of the downloaded archive, if the server provided one
     */
    fileName?: string;
}

export const StateSaved = ({ fileName }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.Done}>
                <svg className={styles.DoneIcon} viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="8" r="7" />
                    <path d="m5 8.2 2 2 4-4.2" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                <div className={styles.DoneText}>
                    <span className={styles.SavedTitle}>{t("Details.DownloadState.saved")}</span>
                    {fileName && <span className={styles.FileName}>{fileName}</span>}
                </div>
            </div>
        </>
    );
};
