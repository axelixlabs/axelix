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
import { Button } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import type { IGCLoggingStatusResponseBody } from "models";

import { GCLogEnableSettings } from "../GCLogEnableSettings";

import styles from "./styles.module.css";

import { InfoIcon, OnOffIcon } from "assets";

interface IProps {
    /**
     * State of GC logging status
     */
    loggingStatusData: IGCLoggingStatusResponseBody;

    /**
     * Loads the GC logging status
     */
    loadGCStatus: () => void;
}

export const GCDisabledMessage = ({ loggingStatusData, loadGCStatus }: IProps) => {
    const { t } = useTranslation();

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.ContentWrapper}>
                    <div className={styles.WarningIconWrapper}>
                        <InfoIcon color="#1890ff" className={styles.InfoIcon} />
                    </div>
                    <div className={`TextLarge ${styles.Title}`}>{t("GC.disableTitle")}</div>
                    <div className={styles.SubTitle}>{t("GC.disableSubtitle")}</div>
                    <Button icon={<OnOffIcon />} type="primary" onClick={() => setIsModalOpen(true)}>
                        {t("GC.enableButtonText")}
                    </Button>
                </div>
                {isModalOpen && (
                    <GCLogEnableSettings
                        isModalOpen={isModalOpen}
                        setIsModalOpen={setIsModalOpen}
                        logginsStatus={loggingStatusData}
                        loadGCStatus={loadGCStatus}
                    />
                )}
            </div>
        </>
    );
};
