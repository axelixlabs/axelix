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
import { Button, Tooltip } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { DownloadIcon } from "@/assets";
import { downloadFile, extractFileName } from "@/helpers";
import { downloadDependenciesSbom } from "@/services";

import styles from "./styles.module.css";

export const DownloadSbom = () => {
    const { instanceId } = useParams();
    const { t } = useTranslation();

    const [downloading, setDownloading] = useState(false);

    const download = (): void => {
        if (downloading) {
            return;
        }

        setDownloading(true);

        downloadDependenciesSbom(instanceId!)
            .then((response) => {
                const name =
                    extractFileName(response.headers["content-disposition"]) ?? t("DependenciesAnalyzer.sbom.fileName");
                downloadFile(response.data, name);
            })
            .finally(() => {
                setDownloading(false);
            });
    };

    return (
        <div className={styles.Action}>
            <div className={styles.Button}>
                <Tooltip title={downloading ? undefined : t("DependenciesAnalyzer.sbom.tooltip")}>
                    <Button
                        icon={<DownloadIcon />}
                        loading={downloading}
                        onClick={download}
                        className={downloading ? styles.Busy : undefined}
                    >
                        {t(
                            downloading
                                ? "DependenciesAnalyzer.sbom.downloading"
                                : "DependenciesAnalyzer.sbom.download",
                        )}
                    </Button>
                </Tooltip>
            </div>
            <span className={styles.FileMeta}>
                <strong>{t("DependenciesAnalyzer.sbom.format")}</strong>
                <span aria-hidden="true"> · </span>
                {t("DependenciesAnalyzer.sbom.fileName")}
            </span>
        </div>
    );
};
