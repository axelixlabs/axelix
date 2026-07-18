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
import { installStepNames } from "@/utils";

import { Dispatch, SetStateAction } from "react";

import styles from "./styles.module.css";

interface IProps {
    installStep: 1 | 2 | 3 | 4;
    setInstallStep: Dispatch<SetStateAction<1 | 2 | 3 | 4>>;
}

export const InstallerBoardFooter = ({ installStep, setInstallStep }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            <button
                className={`${styles.NavButton} ${styles.BackButton}`}
                type="button"
                disabled={installStep <= 1}
                onClick={() => {
                    if (installStep > 1) {
                        setInstallStep((installStep - 1) as 1 | 2 | 3 | 4);
                    }
                }}
            >
                ← {installStep > 1 ? installStepNames[(installStep - 1) as 1 | 2 | 3 | 4] : "Previous"}
            </button>
            <div className={styles.Status}>Step {installStep} of 4</div>
            <button
                className={`${styles.NavButton} ${styles.NextButton}`}
                type="button"
                disabled={installStep === 4}
                onClick={() => {
                    setInstallStep((installStep + 1) as 1 | 2 | 3 | 4);
                }}
            >
                {installStepNames[(installStep + 1) as 1 | 2 | 3 | 4]} →
            </button>
        </div>
    );
};
