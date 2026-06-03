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
"use client";
import { BareMetal } from "./Snippets/BareMetal";
import { ComposeSnippet } from "./Snippets/ComposeSnippet";
import { DockerSnippet } from "./Snippets/DockerSnippet";
import { K8sSnippet } from "./Snippets/K8sSnippet";
import { useEffect, useRef, useState } from "react";

import { CfgVariant, Method, SbVariant } from "../../../../models";

import { CopySnippet } from "./CopySnippet";
import { InstallThirdStep } from "./InstallThirdStep";
import { InstallerBoardFooter } from "./InstallerBoardFooter";
import { InstallerBoardHeader } from "./InstallerBoardHeader";
import { InstallerFooter } from "./InstallerFooter";
import { InstallerMethods } from "./InstallerMethods";
import { StarterMini } from "./StarterMini";
import styles from "./styles.module.css";

const SB_ARTIFACT: Record<SbVariant, string> = {
    sb2: "axelix-spring-boot-2-starter",
    sb3: "axelix-spring-boot-3-starter",
    sb4: "axelix-spring-boot-4-starter",
};

const STEP_NAMES: Record<1 | 2 | 3, string> = {
    1: "Run Axelix",
    2: "Add Starter",
    3: "Configure",
};

export const Installer = () => {
    const [method, setMethod] = useState<Method>("docker");
    const [step, setStep] = useState<1 | 2 | 3>(1);
    const [sb, setSb] = useState<SbVariant>("sb2");
    const [cfg, setCfg] = useState<CfgVariant>("yaml");
    const activeSnippetRef = useRef<HTMLElement | null>(null);

    const [openSelect, setOpenSelect] = useState<null | "sb" | "cfg">(null);

    const selectRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        function onClick(e: MouseEvent) {
            if (!selectRef.current) {
                return;
            }
            if (!selectRef.current.contains(e.target as Node)) {
                setOpenSelect(null);
            }
        }
        document.addEventListener("click", onClick);
        return () => document.removeEventListener("click", onClick);
    }, []);

    return (
        <>
            <div className={styles.InstallerWrapper}>
                <InstallerMethods method={method} setMethod={setMethod} />

                <div className={styles.InstallerBoardWrapper}>
                    <div className={styles.InstallerBoardInnerWrapper}>
                        <InstallerBoardHeader
                            step={step}
                            setStep={setStep}
                            STEP_NAMES={STEP_NAMES}
                            selectRef={selectRef}
                            openSelect={openSelect}
                            setOpenSelect={setOpenSelect}
                            setCfg={setCfg}
                            setSb={setSb}
                            sb={sb}
                            cfg={cfg}
                        />

                        <div className={styles.InstallerBoard}>
                            {step !== 2 && <CopySnippet activeSnippetRef={activeSnippetRef} />}

                            {step === 1 && method === "docker" && <DockerSnippet refEl={activeSnippetRef} />}
                            {step === 1 && method === "compose" && <ComposeSnippet refEl={activeSnippetRef} />}
                            {step === 1 && method === "k8s" && <K8sSnippet refEl={activeSnippetRef} />}
                            {step === 1 && method === "bare" && <BareMetal refEl={activeSnippetRef} />}

                            {step === 2 && (
                                <StarterMini artifact={SB_ARTIFACT[sb]} activeSnippetRef={activeSnippetRef} />
                            )}

                            {step === 3 && (
                                <InstallThirdStep method={method} cfg={cfg} activeSnippetRef={activeSnippetRef} />
                            )}
                        </div>

                        <InstallerBoardFooter step={step} setStep={setStep} STEP_NAMES={STEP_NAMES} />
                    </div>
                </div>
            </div>
            <InstallerFooter />
        </>
    );
};
