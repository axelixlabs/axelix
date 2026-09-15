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

import { HintTooltip } from "@/components";
import type { IDependencySupportSignal } from "@/models";
import { supportSignalClassToken, supportSignalColor, supportSignalLabelKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The signal the curated project registry raised about the dependency.
     */
    signal: IDependencySupportSignal;
}

/**
 * The signal a flagged dependency carries in the feed. Hovering it reveals why the project carries the signal and
 * what that was read from.
 */
export const SupportSignalChip = ({ signal }: IProps) => {
    const { t } = useTranslation();

    return (
        <HintTooltip
            content={
                <>
                    <div className={styles.HintHeader}>
                        <span className={styles.HintDot} style={{ backgroundColor: supportSignalColor[signal.kind] }} />
                        <span className={styles.HintTitle} style={{ color: supportSignalColor[signal.kind] }}>
                            {t(supportSignalLabelKey[signal.kind])}
                        </span>
                    </div>
                    <div>{signal.reason}</div>
                    <div className={styles.Evidence}>
                        <span className={styles.EvidenceLabel}>{t("DependenciesAnalyzer.detail.detectedFrom")}</span>
                        <span className={styles.EvidenceValue}>{signal.evidence}</span>
                    </div>
                </>
            }
        >
            <span className={`TextUltraSmall ${styles.Chip} ${styles[supportSignalClassToken[signal.kind]]}`}>
                <span className={styles.Dot} />
                {t(supportSignalLabelKey[signal.kind])}
            </span>
        </HintTooltip>
    );
};
