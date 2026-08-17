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

import type { EAssociationProblem } from "@/models";
import { ASSOCIATION_PROBLEM_ORDER, associationProblemClassToken, associationProblemLabelKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The currently active problem-type filters.
     */
    activeProblemTypes: EAssociationProblem[];

    /**
     * Toggles a problem type in the active filters.
     */
    onToggle: (type: EAssociationProblem) => void;
}

export const AssociationProblemFilter = ({ activeProblemTypes, onToggle }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                <span className={styles.Caption}>{t("EntitiesMap.problemType")}</span>

                {ASSOCIATION_PROBLEM_ORDER.map((type) => {
                    const active = activeProblemTypes.includes(type);

                    return (
                        <button
                            key={type}
                            type="button"
                            onClick={() => onToggle(type)}
                            className={`${styles.Chip} ${styles[associationProblemClassToken[type]]} ${active ? styles.ActiveChip : ""}`}
                        >
                            <span className={styles.ChipDot} />
                            {t(associationProblemLabelKey[type])}
                        </button>
                    );
                })}
            </div>
        </>
    );
};
