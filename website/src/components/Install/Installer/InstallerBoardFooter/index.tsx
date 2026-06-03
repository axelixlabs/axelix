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
import { Dispatch, SetStateAction } from "react";

import styles from "./styles.module.css";

interface IProps {
    step: 1 | 2 | 3;
    setStep: Dispatch<SetStateAction<1 | 2 | 3>>;
    STEP_NAMES: any;
}

export const InstallerBoardFooter = ({ step, setStep, STEP_NAMES }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            <button
                className={`${styles.NavButton} ${styles.BackButton}`}
                type="button"
                disabled={step <= 1}
                onClick={() => {
                    if (step > 1) {
                        setStep((step - 1) as 1 | 2 | 3);
                    }
                }}
            >
                ← {step > 1 ? STEP_NAMES[(step - 1) as 1 | 2 | 3] : "Previous"}
            </button>
            <div className={styles.Status}>Step {step} of 3</div>
            <button
                className={`${styles.NavButton} ${styles.NextButton}`}
                type="button"
                disabled={step === 3}
                onClick={() => {
                    setStep((step + 1) as 1 | 2 | 3);
                }}
            >
                {STEP_NAMES[(step + 1) as 1 | 2 | 3]} →
            </button>
        </div>
    );
};
