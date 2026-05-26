"use client"
import styles from "./styles.module.css"
import { useEffect, useRef, useState } from "react";
import { InstallerMethods } from "./InstallerMethods";
import { CfgVariant, Method, SbVariant } from "../../../../models";
import { InstallerBoardHeader } from "./InstallerBoardHeader";
import { DockerSnippet } from "./Snippets/DockerSnippet";
import { ComposeSnippet } from "./Snippets/ComposeSnippet";
import { K8sSnippet } from "./Snippets/K8sSnippet";
import { BareMetal } from "./Snippets/BareMetal";
import { YamlSnippet } from "./Snippets/YamlSnippet";
import { PropertiesSnippet } from "./Snippets/PropertiesSnippet";
import { CopySnippet } from "./CopySnippet";
import { StarterMini } from "./StarterMini";
import { InstallerFooter } from "./InstallerFooter";
import { InstallerBoardFooter } from "./InstallerBoardFooter";
import { InstallThirdStep } from "./InstallThirdStep";

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
                        <InstallerBoardHeader step={step} setStep={setStep} STEP_NAMES={STEP_NAMES} selectRef={selectRef} openSelect={openSelect} setOpenSelect={setOpenSelect} setCfg={setCfg} setSb={setSb} sb={sb} cfg={cfg} />

                        <div className={styles.InstallerBoard}>
                            {step !== 2 && <CopySnippet activeSnippetRef={activeSnippetRef} />}

                            {step === 1 && method === "docker" && (
                                <DockerSnippet refEl={activeSnippetRef} />
                            )}
                            {step === 1 && method === "compose" && (
                                <ComposeSnippet refEl={activeSnippetRef} />
                            )}
                            {step === 1 && method === "k8s" && (
                                <K8sSnippet refEl={activeSnippetRef} />
                            )}
                            {step === 1 && method === "bare" && (
                                <BareMetal refEl={activeSnippetRef} />
                            )}

                            {step === 2 && (
                                <StarterMini artifact={SB_ARTIFACT[sb]} activeSnippetRef={activeSnippetRef} />
                            )}

                            {step === 3 && (
                                <InstallThirdStep
                                    method={method}
                                    cfg={cfg}
                                    activeSnippetRef={activeSnippetRef}
                                />
                            )}
                        </div>

                        <InstallerBoardFooter step={step} setStep={setStep} STEP_NAMES={STEP_NAMES} />
                    </div>
                </div>
            </div>
            <InstallerFooter />
        </>
    )
} 