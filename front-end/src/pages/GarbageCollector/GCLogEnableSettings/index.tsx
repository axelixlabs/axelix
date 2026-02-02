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
import { Select } from "antd";
import type { AxiosError } from "axios";
import { type Dispatch, type SetStateAction, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { UniversalModal } from "components";
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
        <UniversalModal
            title={t("GC.modalWindow.title")}
            open={isModalOpen}
            onOk={onOk}
            displayCancel={false}
            onClose={onClose}
            okText={t("GC.modalWindow.submit")}
            loading={enableGCLoggingData.loading}
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
        </UniversalModal>
    );
};
