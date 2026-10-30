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
import { Modal } from "antd";
import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { downloadFile, extractFileName } from "@/helpers";
import type { EExportableComponent, TStateExportStage } from "@/models";
import { exportStateData } from "@/services";
import { EXPORTABLE_COMPONENT_GROUPS } from "@/utils";

import { DownloadStateFooter } from "./DownloadStateFooter";
import { StateChecklist } from "./StateChecklist";
import { StateCollecting } from "./StateCollecting";
import { StateSaved } from "./StateSaved";
import styles from "./styles.module.css";

interface IProps {
    /**
     * Whether the modal is open
     */
    open: boolean;

    /**
     * Callback to close the modal
     */
    onClose: () => void;
}

const ALL_COMPONENTS = EXPORTABLE_COMPONENT_GROUPS.flatMap((group) => group.components);

// The Spring components are small and safe to collect, the JVM ones are opt-in
const DEFAULT_SELECTION = EXPORTABLE_COMPONENT_GROUPS.find((group) => group.key === "spring")!.components;

export const DownloadStateModal = ({ open, onClose }: IProps) => {
    const { instanceId } = useParams();
    const { t } = useTranslation();

    const [selected, setSelected] = useState<EExportableComponent[]>(DEFAULT_SELECTION);
    const [stage, setStage] = useState<TStateExportStage>("pick");
    const [fileName, setFileName] = useState<string>();
    const abortController = useRef<AbortController>(null);

    useEffect(() => {
        if (open) {
            setSelected(DEFAULT_SELECTION);
            setStage("pick");
        }
    }, [open]);

    const toggle = (components: EExportableComponent[], on: boolean): void => {
        // Keep the selection in the dialog order, so the progress list matches the checklist
        setSelected((prev) =>
            ALL_COMPONENTS.filter((component) => (components.includes(component) ? on : prev.includes(component))),
        );
    };

    const close = (): void => {
        abortController.current?.abort();
        onClose();
    };

    const start = (): void => {
        const controller = new AbortController();
        abortController.current = controller;
        setStage("run");

        exportStateData(
            { instanceId: instanceId!, body: { components: selected.map((component) => ({ component })) } },
            controller.signal,
        )
            .then((response) => {
                const name = extractFileName(response.headers["content-disposition"]);
                downloadFile(response.data, name);
                setFileName(name);
                setStage("done");
            })
            .catch(() => {
                // Either cancelled by the user, or the error is already reported by the api interceptor
                if (!controller.signal.aborted) {
                    setStage("pick");
                }
            });
    };

    return (
        <>
            <Modal
                open={open}
                onCancel={close}
                centered
                width={560}
                mask={{ closable: false, blur: true }}
                title={
                    <div className={styles.Title}>
                        <span className="TextMedium">{t("Details.DownloadState.title")}</span>
                        <span className={styles.Subtitle}>{t(`Details.DownloadState.subtitle.${stage}`)}</span>
                    </div>
                }
                footer={
                    <DownloadStateFooter
                        stage={stage}
                        selected={selected}
                        total={ALL_COMPONENTS.length}
                        onClose={close}
                        onStart={start}
                    />
                }
            >
                {stage === "pick" && <StateChecklist selected={selected} onToggle={toggle} />}
                {stage === "run" && <StateCollecting components={selected} />}
                {stage === "done" && <StateSaved fileName={fileName} />}
            </Modal>
        </>
    );
};
