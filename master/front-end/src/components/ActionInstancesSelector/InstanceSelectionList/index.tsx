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
import { Checkbox } from "antd";
import { useTranslation } from "react-i18next";

import type { IProps as ISelectorProps } from "../index";

import { InstanceSelectionRow } from "./InstanceSelectionRow";
import styles from "./styles.module.css";

interface IProps extends Omit<ISelectorProps, "currentInstance" | "totalInstances" | "onOpen"> {
    /** The current instance cannot be deselected */
    currentInstance: NonNullable<ISelectorProps["currentInstance"]>;

    /** Accessible popup identifier */
    popupId: string;
}

export const InstanceSelectionList = ({
    currentInstance,
    instances,
    selectedInstanceIds,
    onSelectionChange,
    popupId,
}: IProps) => {
    const { t } = useTranslation();
    const otherInstances = instances.filter((instance) => instance.instanceId !== currentInstance.instanceId);
    const selectedOthers = otherInstances.filter((instance) => selectedInstanceIds.includes(instance.instanceId));
    const allSelected = otherInstances.length > 0 && selectedOthers.length === otherInstances.length;

    const toggleInstance = (instanceId: string): void => {
        const selected = selectedOthers.map((instance) => instance.instanceId);
        const nextSelection = selected.includes(instanceId)
            ? selected.filter((id) => id !== instanceId)
            : [...selected, instanceId];
        onSelectionChange([currentInstance.instanceId, ...nextSelection]);
    };

    const toggleAll = (): void => {
        onSelectionChange(
            allSelected ? [currentInstance.instanceId] : instances.map((instance) => instance.instanceId),
        );
    };

    return (
        <div id={popupId} role="dialog" aria-label={t("InstanceContext.instances")} className={styles.MainWrapper}>
            <div className={styles.Current}>
                <InstanceSelectionRow instance={currentInstance} current />
            </div>
            <div className={styles.SelectAll}>
                <Checkbox
                    checked={allSelected}
                    indeterminate={selectedOthers.length > 0 && !allSelected}
                    disabled={otherInstances.length === 0}
                    onChange={toggleAll}
                    className={styles.AllCheckbox}
                >
                    {t("ActionInstancesSelector.selectAllOthers")}
                </Checkbox>
            </div>
            <div className={styles.Options}>
                {otherInstances.map((instance) => (
                    <InstanceSelectionRow
                        key={instance.instanceId}
                        instance={instance}
                        selected={selectedInstanceIds.includes(instance.instanceId)}
                        onToggle={toggleInstance}
                    />
                ))}
            </div>
            <div className={styles.Footer} role="status" aria-live="polite">
                {t("ActionInstancesSelector.selectedCount", {
                    count: selectedOthers.length + 1,
                    total: instances.length,
                })}
            </div>
        </div>
    );
};
