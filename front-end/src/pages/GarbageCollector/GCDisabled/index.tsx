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
import { Button } from "antd";
import InfoIcon from "assets/icons/info.svg?react";
import OnOffIcon from "assets/icons/onOf.svg?react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";
import { useState } from "react";
import { GCSettings } from "../GCSettings";
import type { IGCLoggingStatusResponseBody, StatefulRequest } from "models";


interface IProps {
    loggingStatusData: StatefulRequest<IGCLoggingStatusResponseBody>;
    loadGCStatus: () => void
}

export const GCDisabled = ({ loggingStatusData, loadGCStatus }: IProps) => {
    const { t } = useTranslation();

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.ContentWrapper}>
                <div className={styles.WarningIconWrapper}>
                    <InfoIcon color="#1890ff" className={styles.InfoIcon} />
                </div>
                <div className={`TextMedium ${styles.Title}`}>{t("GC.disableTitle")}</div>
                <div className={styles.SubTitle}>{t("GC.disableSubtitle")}</div>
                <Button
                    icon={<OnOffIcon />}
                    type="primary"
                    className={styles.CloseButton}
                    onClick={() => setIsModalOpen(true)}
                >
                    {t("GC.enableButtonText")}
                </Button>
            </div>
            {isModalOpen && <GCSettings
                isModalOpen={isModalOpen}
                setIsModalOpen={setIsModalOpen}
                logginStatusData={loggingStatusData}
                loadGCStatus={loadGCStatus}
            />}
        </div>
    );
};
