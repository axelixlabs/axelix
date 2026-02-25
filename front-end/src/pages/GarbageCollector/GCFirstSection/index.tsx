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
import { App, Button, Tooltip } from "antd";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { downloadFile, extractErrorCode } from "helpers";
import { type IErrorResponse, StatelessRequest } from "models";
import { disableGCLogging, getGCLogFile, triggerGC } from "services";

import styles from "./styles.module.css";

import { DownloadIcon, OnOffIcon, RunIcon } from "assets";

export interface IProps {
    /**
     * Loads the GC logging status
     */
    loadGCStatus: () => void;

    /**
     * Indicates whether GC logging is enabled
     */
    isLoggingStatusEnabled: boolean;
}

export const GCFirstSection = ({ loadGCStatus, isLoggingStatusEnabled }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();

    const [disableGCData, setDisableGCData] = useState(StatelessRequest.inactive());
    const [triggerGBData, setTriggerGBData] = useState(StatelessRequest.inactive());
    const [downloadFileLoading, setDownloadFileLoading] = useState<boolean>(false);

    const disableGCHandler = (): void => {
        setDisableGCData(StatelessRequest.loading());

        disableGCLogging(instanceId!)
            .then(() => {
                setDisableGCData(StatelessRequest.success());
                loadGCStatus();
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setDisableGCData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    const triggerGBHandler = (): void => {
        setTriggerGBData(StatelessRequest.loading());

        triggerGC(instanceId!)
            .then(() => {
                setTriggerGBData(StatelessRequest.success());
                message.success(t("GC.triggered"));
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setTriggerGBData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    const downloadFileHandler = (): void => {
        setDownloadFileLoading(true);

        getGCLogFile(instanceId!)
            .then((responseBody) => {
                const file = responseBody.data;
                downloadFile(file);
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setTriggerGBData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            })
            .finally(() => {
                setDownloadFileLoading(false);
            });
    };

    return (
        <>
            <div className={styles.FirstSection}>
                <div className="TextLarge">{t("GC.mainTitle")}</div>
                <div className={styles.ActionButtonsWrapper}>
                    {isLoggingStatusEnabled && (
                        <>
                            <Tooltip title={t("GC.download")}>
                                <Button
                                    icon={<DownloadIcon />}
                                    type="primary"
                                    loading={downloadFileLoading}
                                    onClick={downloadFileHandler}
                                    className={styles.ActionButton}
                                />
                            </Tooltip>
                            <Tooltip title={t("GC.disable")}>
                                <Button
                                    icon={<OnOffIcon />}
                                    type="primary"
                                    loading={disableGCData.loading}
                                    onClick={disableGCHandler}
                                    danger
                                    className={styles.ActionButton}
                                />
                            </Tooltip>
                        </>
                    )}
                    <Tooltip title={t("GC.triggerButtonText")}>
                        <Button
                            icon={<RunIcon />}
                            type="primary"
                            loading={triggerGBData.loading}
                            onClick={triggerGBHandler}
                            className={styles.ActionButton}
                        />
                    </Tooltip>
                </div>
            </div>
        </>
    );
};
