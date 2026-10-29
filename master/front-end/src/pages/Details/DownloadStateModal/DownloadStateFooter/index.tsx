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
import { Button } from "antd";
import { useTranslation } from "react-i18next";

import { DownloadIcon } from "@/assets";
import { EExportableComponent } from "@/models";

import styles from "../styles.module.css";

interface IProps {
    /**
     * The current phase of the export
     */
    phase: "pick" | "run" | "done";

    /**
     * The selected components
     */
    chosen: EExportableComponent[];

    /**
     * The total number of exportable components
     */
    total: number;

    /**
     * Callback to close the dialog
     */
    onClose: () => void;

    /**
     * Callback to start the export
     */
    onStart: () => void;
}

export const DownloadStateFooter = ({ phase, chosen, total, onClose, onStart }: IProps) => {
    const { t } = useTranslation();

    const withHeap = chosen.includes(EExportableComponent.HEAP_DUMP);
    // Everything but the heap dump stays well below a megabyte
    const smallCount = chosen.length - (withHeap ? 1 : 0);

    let sizeLabel = smallCount ? t("Details.DownloadState.size.small", { mb: smallCount }) : "—";
    if (withHeap) {
        sizeLabel = smallCount
            ? t("Details.DownloadState.size.heapPlus", { mb: smallCount })
            : t("Details.DownloadState.size.heap");
    }

    return (
        <>
            <div className={styles.Footer}>
                <div className={styles.Summary}>
                    <div className={styles.Stat}>
                        <span className={styles.StatLabel}>{t("Details.DownloadState.selected")}</span>
                        <span className={styles.StatValue}>
                            {t("Details.DownloadState.count", { selected: chosen.length, total })}
                        </span>
                    </div>
                    <span className={styles.Divider} />
                    <div className={styles.Stat}>
                        <span className={styles.StatLabel}>{t("Details.DownloadState.sizeZipped")}</span>
                        <span className={styles.StatValue}>
                            {sizeLabel}
                            {withHeap && phase === "pick" && <span className={styles.HeapDot} />}
                        </span>
                    </div>
                </div>
                <div className={styles.Actions}>
                    <Button onClick={onClose}>
                        {phase === "done" ? t("Details.DownloadState.close") : t("cancel")}
                    </Button>
                    {phase !== "done" && (
                        <Button
                            type="primary"
                            icon={<DownloadIcon />}
                            disabled={chosen.length === 0}
                            loading={phase === "run"}
                            onClick={onStart}
                        >
                            {t("Details.DownloadState.download")}
                        </Button>
                    )}
                </div>
            </div>
        </>
    );
};
