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
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { UniversalModal } from "components";

import { ContentionMonitoringStatusSwitch } from "./ContentionMonitoringStatusSwitch";
import styles from "./styles.module.css";

interface IProps {
    /**
     * Indicates whether the modal is open
     */
    isModalOpen: boolean;

    /**
     * Setter to update the modal open state
     */
    setIsModalOpen: Dispatch<SetStateAction<boolean>>;

    /**
     * Whether thread contention monitoring is enabled
     */
    contentionMonitoring: boolean;
}

export const ThreadDumpSettingsModal = ({ isModalOpen, setIsModalOpen, contentionMonitoring }: IProps) => {
    const { t } = useTranslation();

    const onClose = (): void => {
        setIsModalOpen(false);
    };

    return (
        <UniversalModal
            title={t("ThreadDump.Settings.title")}
            open={isModalOpen}
            onOk={onClose}
            displayCancel={false}
            onClose={onClose}
        >
            <div className={styles.ModalContentWrapper}>
                <div className={styles.SettingsItemWrapper}>
                    <div>{t("ThreadDump.Settings.contentionMonitoring")}</div>
                    <ContentionMonitoringStatusSwitch contentionMonitoring={contentionMonitoring} />
                </div>
            </div>
        </UniversalModal>
    );
};
