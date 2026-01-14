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
import { Modal, Select } from "antd";
import type { AxiosError } from "axios";
import { type Dispatch, type SetStateAction, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { extractErrorCode } from "helpers";
import { type IErrorResponse, type IGCLoggingStatusResponseBody, StatelessRequest } from "models";
import { enableGCLogging } from "services";
import { getLevelsSelectData } from "utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Whether the modal is currently open
     */
    isModalOpen: boolean;

    /**
     * Setter for updating the modal open state
     */
    setIsModalOpen: Dispatch<SetStateAction<boolean>>;

    /**
     * State of GC logging status
     */
    logginsStatus: IGCLoggingStatusResponseBody;

    /**
     * Loads the GC logging status
     */
    loadGCStatus: () => void;
}

export const GCLogEnableSettings = ({ isModalOpen, setIsModalOpen, logginsStatus, loadGCStatus }: IProps) => {
    const { instanceId } = useParams();
    const { t } = useTranslation();

    const { level, availableLevels } = logginsStatus!;

    const [selectedLevel, setSelectedLevel] = useState<string>(level);
    const [enableGCLoggingData, setEnableGCLoggingData] = useState(StatelessRequest.inactive());

    const onOk = (): void => {
        setEnableGCLoggingData(StatelessRequest.loading());

        enableGCLogging({
            instanceId: instanceId!,
            level: selectedLevel,
        })
            .then(() => {
                setEnableGCLoggingData(StatelessRequest.success());
                loadGCStatus();
                setIsModalOpen(false);
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setEnableGCLoggingData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    const onClose = (): void => {
        setIsModalOpen(false);
    };

    return (
        <Modal
            title={t("GC.modalWindow.title")}
            open={isModalOpen}
            onOk={onOk}
            onCancel={onClose}
            okText={t("GC.modalWindow.submit")}
            centered
            width={550}
            loading={enableGCLoggingData.loading}
            cancelButtonProps={{
                style: { display: "none" },
            }}
        >
            <div className={styles.ContentWrapper}>
                <div>{t("GC.modalWindow.selectLoggingLevel")}:</div>
                <Select
                    placeholder="Level"
                    defaultValue={selectedLevel}
                    onChange={(level) => setSelectedLevel(level)}
                    options={getLevelsSelectData(availableLevels)}
                    className={styles.LoggingLevelSelect}
                />
            </div>
        </Modal>
    );
};
