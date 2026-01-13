/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { App, Button } from "antd";
import DownloadIcon from "assets/icons/download.svg?react";
import OnOffIcon from "assets/icons/onOf.svg?react";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { downloadFile, extractErrorCode, fetchData } from "helpers";
import { type IErrorResponse, StatefulRequest, StatelessRequest } from "models";
import { disableGCLogging, getGCLogFile, triggerGC } from "services";

import styles from "./styles.module.css";

export interface IProps {
    loadGCStatus: () => void;
    isLoggingStatusEnabled: boolean;
}

export const GCFirstSection = ({ loadGCStatus, isLoggingStatusEnabled }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();

    const [disableGCData, setDisableGCData] = useState(StatelessRequest.inactive());
    const [triggerGBData, setTriggerGBData] = useState(StatelessRequest.inactive());
    const [downloadFileLoading, setDownloadFileLoading] = useState<boolean>(false);
    console.log(downloadFileLoading)

    const disableGChandler = (): void => {
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

    const triggerGBhandler = (): void => {
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
        setDownloadFileLoading(true)

        getGCLogFile(instanceId!)
            .then((responseBody) => {
                const file = responseBody.data
                downloadFile(file)
                // setTriggerGBData(StatelessRequest.success());
                // message.success(t("GC.triggered"));
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setTriggerGBData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            }).finally(() => {
                setDownloadFileLoading(false);
            });
    };

    return (
        <div className={styles.FirstSection}>
            <div className="TextMedium">{t("GC.logList")}</div>
            <div className={styles.ActionButtonsWrapper}>
                {isLoggingStatusEnabled && (
                    <>
                        <Button icon={<DownloadIcon />} type="primary" loading={downloadFileLoading} onClick={downloadFileHandler}>
                            {t("GC.download")}
                        </Button>
                        <Button
                            icon={<OnOffIcon />}
                            type="primary"
                            loading={disableGCData.loading}
                            onClick={disableGChandler}
                            danger
                        >
                            {t("GC.disable")}
                        </Button>
                    </>
                )}
                <Button
                    icon={<OnOffIcon />}
                    type="primary"
                    loading={triggerGBData.loading}
                    onClick={triggerGBhandler}
                >
                    {t("GC.triggerButtonText")}
                </Button>
            </div>
        </div>
    );
};
