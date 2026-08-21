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

import { QuestionIcon } from "@/assets";
import type { EAssociationProblem } from "@/models";
import { associationProblemClassToken, associationProblemLabelKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The association problem type to render.
     */
    type: EAssociationProblem;

    /**
     * How many associations exhibit the problem. A suffix is appended when it is greater than one.
     */
    multiplicity?: number;

    /**
     * When set, the chip is rendered as an outlined trigger with a trailing question mark, hinting that hovering it
     * reveals an explanation.
     */
    withHint?: boolean;
}

export const AssociationProblemChip = ({ type, multiplicity, withHint }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <span
                className={`${styles.Chip} ${styles[associationProblemClassToken[type]]} ${withHint ? styles.Outlined : ""}`}
            >
                <span className={styles.Dot} />
                {t(associationProblemLabelKey[type])}
                {multiplicity && multiplicity > 1 ? ` ×${multiplicity}` : ""}
                {withHint && <QuestionIcon className={styles.Hint} />}
            </span>
        </>
    );
};
