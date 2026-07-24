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

import type { EProblemType } from "models";

import { PROBLEM_TYPE_ORDER, problemClassToken, problemLabelKey } from "../problemTypes";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The currently active problem-type filters.
     */
    activeProblemTypes: EProblemType[];

    /**
     * Toggles a problem type in the active filters.
     */
    onToggle: (type: EProblemType) => void;
}

export const ProblemTypeFilter = ({ activeProblemTypes, onToggle }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={styles.Filter}>
            <span className={styles.Caption}>{t("Transactional.problemType")}</span>
            {PROBLEM_TYPE_ORDER.map((type) => {
                const active = activeProblemTypes.includes(type);

                return (
                    <button
                        key={type}
                        type="button"
                        aria-pressed={active}
                        onClick={() => onToggle(type)}
                        className={`${styles.Chip} ${styles[problemClassToken[type]]} ${active ? styles.Active : ""}`}
                    >
                        <span className={styles.Dot} />
                        {t(problemLabelKey[type])}
                    </button>
                );
            })}
        </div>
    );
};
