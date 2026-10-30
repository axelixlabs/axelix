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

import { DownloadIcon } from "@/assets";
import { resolveInitials } from "@/helpers";

import { DownloadStateModal } from "../DownloadStateModal";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The name of the instance
     */
    instanceName: string;

    /**
     * The group ID of the application
     */
    groupId: string;
}

export const DetailsHeader = ({ instanceName, groupId }: IProps) => {
    const { t } = useTranslation();

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.Identity}>
                    <span className={styles.Initials}>{resolveInitials(instanceName)}</span>
                    <div className={styles.Names}>
                        <span className={`TextMedium ${styles.MainTitle}`}>{instanceName}</span>
                        {groupId && (
                            <span className={styles.GroupId}>
                                groupId: <span className={styles.GroupIdValue}>{groupId}</span>
                            </span>
                        )}
                    </div>
                </div>
                <Button icon={<DownloadIcon />} onClick={() => setIsModalOpen(true)} className={styles.Download}>
                    {t("Details.downloadState")}
                </Button>
                <DownloadStateModal open={isModalOpen} onClose={() => setIsModalOpen(false)} />
            </div>
        </>
    );
};
