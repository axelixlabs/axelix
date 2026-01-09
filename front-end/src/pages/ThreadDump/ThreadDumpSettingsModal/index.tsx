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
import { Modal } from "antd";
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { ContentionMonitoringStatusSwitch } from "./ContentionMonitoringStatusSwitch";
import styles from "./styles.module.css";

interface IProps {
    /**
     * Indicates whether the modal is open
     */
    isModalopen: boolean;

    /**
     * Setter to update the modal open state
     */
    setIsModalOpen: Dispatch<SetStateAction<boolean>>;

    /**
     * Whether thread contention monitoring is enabled
     */
    contentionMonitoring: boolean;
}

export const ThreadDumpSettingsModal = ({ isModalopen, setIsModalOpen, contentionMonitoring }: IProps) => {
    const { t } = useTranslation();

    const onClose = (): void => {
        setIsModalOpen(false);
    };

    return (
        // TODO: In our project, <Modal> component from аntd is used in several places.
        // In the future, we may consider creating a universal component for it.
        <Modal
            title={t("ThreadDump.Settings.title")}
            open={isModalopen}
            onOk={onClose}
            onCancel={onClose}
            centered
            width={550}
            cancelButtonProps={{
                style: { display: "none" },
            }}
        >
            <div className={styles.ModalContentWrapper}>
                <div className={styles.SettingsItemWrapper}>
                    <div>{t("ThreadDump.Settings.contentionMonitoring")}</div>
                    <ContentionMonitoringStatusSwitch contentionMonitoring={contentionMonitoring} />
                </div>
            </div>
        </Modal>
    );
};
