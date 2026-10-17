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
import { LockOutlined } from "@ant-design/icons";

import { Checkbox } from "antd";
import { useTranslation } from "react-i18next";

import { InstanceCommit, InstanceCurrentTag, InstanceStatusTag } from "@/components/InstanceChips";
import type { TInstanceOption } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    /** Metadata of the instance */
    instance: TInstanceOption;

    /** Whether this is the pinned current instance */
    current?: boolean;

    /** Whether an additional instance is selected */
    selected?: boolean;

    /** Toggles an additional instance */
    onToggle?: (instanceId: string) => void;
}

export const InstanceSelectionRow = ({ instance, current = false, selected = false, onToggle }: IProps) => {
    const { t } = useTranslation();
    const details = (
        <span className={styles.Details} title={instance.name}>
            <span className={styles.Name}>{instance.name}</span>
            <span className={styles.Meta}>
                <InstanceCommit commitShaShort={instance.commitShaShort} />
                {current && <InstanceCurrentTag />}
                <span className={styles.Status}>
                    <InstanceStatusTag status={instance.status} />
                </span>
            </span>
        </span>
    );

    if (current) {
        return (
            <div className={styles.CurrentRow}>
                {details}
                <LockOutlined className={styles.Lock} aria-label={t("ActionInstancesSelector.alwaysIncluded")} />
            </div>
        );
    }

    return (
        <Checkbox
            checked={selected}
            onChange={() => onToggle?.(instance.instanceId)}
            className={`${styles.Checkbox} ${selected ? styles.Selected : ""}`}
            aria-label={t("InstanceContext.selectInstance", { name: instance.name })}
        >
            {details}
        </Checkbox>
    );
};
