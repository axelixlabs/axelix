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

import type { IRankedEcosystem } from "@/models";
import { RISKY_SUPPORT_STATUS_ORDER, dependencyEcosystemLabelKey, dependencyHealthColor } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    category: IRankedEcosystem;

    /**
     * The total of the top category, the bar of which spans the whole track.
     */
    max: number;
    active: boolean;
    onToggle: () => void;
}

export const CategoryBar = ({ category, max, active, onToggle }: IProps) => {
    const { t } = useTranslation();

    const { ecosystem, byStatus, total } = category;

    return (
        <>
            <button type="button" onClick={onToggle} className={`${styles.MainWrapper} ${active ? styles.Active : ""}`}>
                <span className={`TextSmall ${styles.Label}`}>{t(dependencyEcosystemLabelKey[ecosystem])}</span>
                <span className={styles.Track}>
                    <span className={styles.Bar} style={{ width: `${(total * 100) / max}%` }}>
                        {RISKY_SUPPORT_STATUS_ORDER.map((status) => (
                            <span
                                key={status}
                                style={{ flex: byStatus[status] ?? 0, backgroundColor: dependencyHealthColor[status] }}
                            />
                        ))}
                    </span>
                </span>
                <span className={`TextSmall ${styles.Count}`}>{total}</span>
            </button>
        </>
    );
};
