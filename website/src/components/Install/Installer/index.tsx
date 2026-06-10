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
import {
    EInstallConfigurationVariant,
    EInstallMethod,
    EInstallOpenSelect,
    ESpringBootVariant,
    IAxelixVersionData,
    IGithubReleaseResponseBody,
} from "@/models";
import { installSpringBootArtifact } from "@/utils";

import { BareMetal } from "./Snippets/BareMetal";
import { ComposeSnippet } from "./Snippets/ComposeSnippet";
import { DockerSnippet } from "./Snippets/DockerSnippet";
import { K8sSnippet } from "./Snippets/K8sSnippet";
import { useEffect, useRef, useState } from "react";

import { CopySnippet } from "./CopySnippet";
import { InstallThirdStep } from "./InstallThirdStep";
import { InstallerBoardFooter } from "./InstallerBoardFooter";
import { InstallerBoardHeader } from "./InstallerBoardHeader";
import { InstallerFooter } from "./InstallerFooter";
import { InstallerMethods } from "./InstallerMethods";
import { PluginMini } from "./PluginMini";
import { StarterMini } from "./StarterMini";
import styles from "./styles.module.css";

export const Installer = () => {
    const [installMethod, setInstallMethod] = useState<EInstallMethod>(EInstallMethod.DOCKER);

    const [installStep, setInstallStep] = useState<1 | 2 | 3 | 4>(1);
    const [springBootVariant, setSpringBootVariant] = useState<ESpringBootVariant>(ESpringBootVariant.SPRING_BOOT_2);
    const [installConfiguration, setInstallConfiguration] = useState<EInstallConfigurationVariant>(
        EInstallConfigurationVariant.YAML,
    );
    const activeSnippetRef = useRef<HTMLElement>(null);

    const [openSelect, setOpenSelect] = useState<EInstallOpenSelect>(EInstallOpenSelect.NULL);
    const [axelixVersionData, setAxelixVersionData] = useState<IAxelixVersionData>({
        version: null,
        loading: true,
    });

    const selectRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        function onClick(e: MouseEvent) {
            if (!selectRef.current) {
                return;
            }

            if (!selectRef.current.contains(e.target as Node)) {
                setOpenSelect(EInstallOpenSelect.NULL);
            }
        }
        document.addEventListener("click", onClick);
        return () => document.removeEventListener("click", onClick);
    }, []);

    // TODO: In the future, split this component into smaller components
    useEffect(() => {
        async function fetchAxelixVersion() {
            try {
                setAxelixVersionData((prev) => ({
                    ...prev,
                    loading: true,
                }));

                const response = await fetch("https://api.github.com/repos/axelixlabs/axelix/releases/latest");

                if (!response.ok) {
                    throw new Error();
                }

                const data: IGithubReleaseResponseBody = await response.json();
                const version = data.tag_name;

                setAxelixVersionData((prev) => ({
                    ...prev,
                    version: version,
                }));
            } catch {
                setAxelixVersionData((prev) => ({
                    ...prev,
                    version: "<VERSION>",
                }));
            } finally {
                setAxelixVersionData((prev) => ({
                    ...prev,
                    loading: false,
                }));
            }
        }

        fetchAxelixVersion();
    }, []);

    return (
        <>
            <div className={styles.InstallerWrapper}>
                <InstallerMethods installMethod={installMethod} setInstallMethod={setInstallMethod} />

                <div className={styles.InstallerBoardWrapper}>
                    <div className={styles.InstallerBoardInnerWrapper}>
                        <InstallerBoardHeader
                            installStep={installStep}
                            setInstallStep={setInstallStep}
                            selectRef={selectRef}
                            openSelect={openSelect}
                            setOpenSelect={setOpenSelect}
                            setInstallConfiguration={setInstallConfiguration}
                            setSpringBootVariant={setSpringBootVariant}
                            springBootVariant={springBootVariant}
                            installConfiguration={installConfiguration}
                        />

                        <div className={styles.InstallerBoard}>
                            {installStep !== 2 && installStep !== 3 && (
                                <CopySnippet activeSnippetRef={activeSnippetRef} />
                            )}

                            {installStep === 1 && installMethod === EInstallMethod.DOCKER && (
                                <DockerSnippet refEl={activeSnippetRef} axelixVersionData={axelixVersionData} />
                            )}
                            {installStep === 1 && installMethod === EInstallMethod.COMPOSE && (
                                <ComposeSnippet refEl={activeSnippetRef} axelixVersionData={axelixVersionData} />
                            )}
                            {installStep === 1 && installMethod === EInstallMethod.K8S && (
                                <K8sSnippet refEl={activeSnippetRef} />
                            )}
                            {installStep === 1 && installMethod === EInstallMethod.BARE && (
                                <BareMetal refEl={activeSnippetRef} />
                            )}

                            {installStep === 2 && (
                                <StarterMini
                                    artifact={installSpringBootArtifact[springBootVariant]}
                                    activeSnippetRef={activeSnippetRef}
                                />
                            )}

                            {installStep === 3 && <PluginMini activeSnippetRef={activeSnippetRef} />}

                            {installStep === 4 && (
                                <InstallThirdStep
                                    installMethod={installMethod}
                                    installConfiguration={installConfiguration}
                                    activeSnippetRef={activeSnippetRef}
                                />
                            )}
                        </div>

                        <InstallerBoardFooter installStep={installStep} setInstallStep={setInstallStep} />
                    </div>
                </div>
            </div>
            <InstallerFooter />
        </>
    );
};
