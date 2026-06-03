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

import { CfgVariant, SbVariant } from "../../../../models";

import { InstallerSelect } from "./InstallerSelect";
import styles from "./styles.module.css";

const SB_OPTIONS: { id: SbVariant; label: string }[] = [
    { id: "sb2", label: "Spring Boot 2" },
    { id: "sb3", label: "Spring Boot 3" },
    { id: "sb4", label: "Spring Boot 4" },
];

const CFG_OPTIONS: { id: CfgVariant; label: string }[] = [
    { id: "yaml", label: "yaml" },
    { id: "properties", label: "properties" },
];

interface IProps {
    step: 1 | 2 | 3;
    setStep: Dispatch<SetStateAction<1 | 2 | 3>>;
    STEP_NAMES: Record<number, string>;
    selectRef: any;
    openSelect: null | "sb" | "cfg";
    setOpenSelect: Dispatch<SetStateAction<null | "sb" | "cfg">>;
    setCfg: Dispatch<SetStateAction<CfgVariant>>;
    cfg: CfgVariant;
    sb: SbVariant;
    setSb: Dispatch<SetStateAction<SbVariant>>;
}

export const InstallerBoardHeader = ({
    step,
    setStep,
    STEP_NAMES,
    selectRef,
    openSelect,
    setOpenSelect,
    setCfg,
    setSb,
    sb,
    cfg,
}: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            <div className={styles.TabsWrapper}>
                {([1, 2, 3] as const).map((s) => (
                    <button
                        key={s}
                        type="button"
                        className={`${styles.Tab} ${step === s ? styles.ActiveTab : ""}`}
                        onClick={() => setStep(s)}
                    >
                        {s}. {STEP_NAMES[s]}
                    </button>
                ))}
            </div>
            <div className={styles.FileInfo} ref={selectRef}>
                {step === 1 && <span className={styles.Label}>Shell</span>}
                {step === 2 && (
                    <InstallerSelect
                        label={SB_OPTIONS.find((o) => o.id === sb)!.label}
                        open={openSelect === "sb"}
                        onToggle={() => {
                            setOpenSelect(openSelect === "sb" ? null : "sb");
                        }}
                        options={SB_OPTIONS}
                        active={sb}
                        onPick={(id) => {
                            setSb(id as SbVariant);
                            setOpenSelect(null);
                        }}
                    />
                )}
                {step === 3 && (
                    <InstallerSelect
                        label={cfg}
                        open={openSelect === "cfg"}
                        onToggle={() => {
                            setOpenSelect(openSelect === "cfg" ? null : "cfg");
                        }}
                        options={CFG_OPTIONS}
                        active={cfg}
                        onPick={(id) => {
                            setCfg(id as CfgVariant);
                            setOpenSelect(null);
                        }}
                    />
                )}
            </div>
        </div>
    );
};
