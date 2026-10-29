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

import { EExportableComponent } from "@/models";

import styles from "../styles.module.css";

interface IProps {
    /**
     * The components being collected
     */
    components: EExportableComponent[];
}

/**
 * The export is a single request, so the progress of the individual components is unknown: all of them
 * are shown as being collected until the archive arrives.
 */
export const StateCollecting = ({ components }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.Run}>
                <div className={styles.Progress}>
                    <div className={styles.ProgressTrack}>
                        <div className={styles.ProgressBar} />
                    </div>
                    <span className={styles.ProgressLabel}>
                        {t("Details.DownloadState.collecting", { count: components.length })}
                    </span>
                </div>
                <div className={styles.RunList}>
                    {components.map((component) => (
                        <div key={component} className={styles.RunItem}>
                            <span className={styles.Spinner} />
                            <span className={styles.ItemName}>{t(`Details.Components.${component}`)}</span>
                            <span className={styles.ItemSize}>
                                {component === EExportableComponent.HEAP_DUMP
                                    ? t("Details.DownloadState.writingHeap")
                                    : t("Details.DownloadState.collectingItem")}
                            </span>
                        </div>
                    ))}
                </div>
            </div>
        </>
    );
};
