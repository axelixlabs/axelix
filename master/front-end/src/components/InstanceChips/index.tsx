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
import { Tag } from "antd";
import { useTranslation } from "react-i18next";

import { EInstanceStatus } from "@/models";

import styles from "./styles.module.css";

interface ICommitProps {
    /** Short commit hash */
    commitShaShort: string;
}

interface IStatusProps {
    /** Instance health status */
    status: EInstanceStatus;
}

export const InstanceCommit = ({ commitShaShort }: ICommitProps) => {
    const { t } = useTranslation();

    return (
        <Tag className={styles.Commit} title={t("InstanceContext.commit")}>
            <svg viewBox="0 0 16 16" aria-hidden="true">
                <path d="M1 8h4m6 0h4" />
                <circle cx="8" cy="8" r="3" />
            </svg>
            {commitShaShort || "—"}
        </Tag>
    );
};

export const InstanceStatusTag = ({ status }: IStatusProps) => {
    const { t } = useTranslation();
    const statusStyle =
        status === EInstanceStatus.DOWN ? styles.Down : status === EInstanceStatus.UNKNOWN ? styles.Unknown : "";

    return (
        <Tag className={`${styles.Status} ${statusStyle}`} aria-label={t(`InstanceContext.status.${status}`)}>
            {status}
        </Tag>
    );
};

export const InstanceCurrentTag = () => {
    const { t } = useTranslation();

    return <Tag className={styles.CurrentTag}>{t("ActionInstancesSelector.current")}</Tag>;
};
