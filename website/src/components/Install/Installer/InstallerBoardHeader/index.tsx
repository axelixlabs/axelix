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
import { EInstallConfigurationVariant, EInstallOpenSelect, ESpringBootVariant } from "@/models";
import { installConfigurationOptions, installSpringBootOptions, installStepNames } from "@/utils";

import { Dispatch, SetStateAction } from "react";

import { InstallerSelect } from "./InstallerSelect";
import styles from "./styles.module.css";

interface IProps {
    installStep: 1 | 2 | 3;
    setInstallStep: Dispatch<SetStateAction<1 | 2 | 3>>;
    selectRef: any;
    openSelect: EInstallOpenSelect;
    setOpenSelect: Dispatch<SetStateAction<EInstallOpenSelect>>;
    setInstallConfiguration: Dispatch<SetStateAction<EInstallConfigurationVariant>>;
    installConfiguration: EInstallConfigurationVariant;
    springBootVariant: ESpringBootVariant;
    setSpringBootVariant: Dispatch<SetStateAction<ESpringBootVariant>>;
}

export const InstallerBoardHeader = ({
    installStep,
    setInstallStep,
    selectRef,
    openSelect,
    setOpenSelect,
    setInstallConfiguration,
    setSpringBootVariant,
    springBootVariant,
    installConfiguration,
}: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            <div className={styles.TabsWrapper}>
                {([1, 2, 3] as const).map((step) => (
                    <button
                        key={step}
                        type="button"
                        className={`${styles.Tab} ${installStep === step ? styles.ActiveTab : ""}`}
                        onClick={() => setInstallStep(step)}
                    >
                        {step}. {installStepNames[step]}
                    </button>
                ))}
            </div>
            <div className={styles.FileInfo} ref={selectRef}>
                {installStep === 1 && <span className={styles.Label}>Shell</span>}
                {installStep === 2 && (
                    <InstallerSelect
                        label={installSpringBootOptions.find((o) => o.key === springBootVariant)!.label}
                        open={openSelect === EInstallOpenSelect.SPRING_BOOT}
                        onToggle={() => {
                            setOpenSelect(
                                openSelect === EInstallOpenSelect.SPRING_BOOT
                                    ? EInstallOpenSelect.NULL
                                    : EInstallOpenSelect.SPRING_BOOT,
                            );
                        }}
                        options={installSpringBootOptions}
                        active={springBootVariant}
                        onPick={(id) => {
                            setSpringBootVariant(id as ESpringBootVariant);
                            setOpenSelect(EInstallOpenSelect.NULL);
                        }}
                    />
                )}
                {installStep === 3 && (
                    <InstallerSelect
                        label={installConfiguration}
                        open={openSelect === EInstallOpenSelect.CONFIGURATION}
                        onToggle={() => {
                            setOpenSelect(
                                openSelect === EInstallOpenSelect.CONFIGURATION
                                    ? EInstallOpenSelect.NULL
                                    : EInstallOpenSelect.CONFIGURATION,
                            );
                        }}
                        options={installConfigurationOptions}
                        active={installConfiguration}
                        onPick={(id) => {
                            setInstallConfiguration(id as EInstallConfigurationVariant);
                            setOpenSelect(EInstallOpenSelect.NULL);
                        }}
                    />
                )}
            </div>
        </div>
    );
};
