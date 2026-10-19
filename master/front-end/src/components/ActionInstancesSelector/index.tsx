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
import { Button, Popover, Tag } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import type { IInstanceOption } from "@/models";

import { InstanceSelectionList } from "./InstanceSelectionList";
import styles from "./styles.module.css";

export interface IProps {
    /** The instance of the page, always included in the selection */
    currentInstance: IInstanceOption | null;

    /** Available preview instances, loaded when the selector opens */
    instances: IInstanceOption[];

    /** How many instances the application has, known before they are loaded */
    totalInstances: number;

    /** Identifiers selected on this page */
    selectedInstanceIds: string[];

    /** Updates the visual selection without executing actions */
    onSelectionChange: (instanceIds: string[]) => void;

    /** Loads the instances, called every time the selector opens */
    onOpen: () => void;
}

export const ActionInstancesSelector = ({
    currentInstance,
    instances,
    totalInstances,
    selectedInstanceIds,
    onSelectionChange,
    onOpen,
}: IProps) => {
    const { t } = useTranslation();
    const [open, setOpen] = useState<boolean>(false);

    // There is nothing to choose from in an application with a single instance
    if (!currentInstance || totalInstances <= 1) {
        return null;
    }

    const handleOpenChange = (visible: boolean): void => {
        setOpen(visible);
        if (visible) {
            onOpen();
        }
    };

    const selectedCount = instances.filter(
        (instance) =>
            instance.instanceId === currentInstance.instanceId || selectedInstanceIds.includes(instance.instanceId),
    ).length;

    return (
        <Popover
            trigger="click"
            placement="bottomRight"
            arrow={false}
            open={open}
            onOpenChange={handleOpenChange}
            destroyOnHidden
            align={{ offset: [0, 8] }}
            classNames={{ root: styles.Popup }}
            styles={{ container: { padding: 0, borderRadius: 9 } }}
            content={
                <InstanceSelectionList
                    currentInstance={currentInstance}
                    instances={instances}
                    selectedInstanceIds={selectedInstanceIds}
                    onSelectionChange={onSelectionChange}
                />
            }
        >
            <Button className={`${styles.Trigger} ${open ? styles.Open : ""}`} aria-expanded={open}>
                <svg className={styles.Stack} viewBox="0 0 24 24" aria-hidden="true">
                    <rect x="4" y="3" width="16" height="7" rx="2" />
                    <rect x="4" y="14" width="16" height="7" rx="2" />
                    <path d="M7 6.5h.01M7 17.5h.01M11 6.5h6M11 17.5h6" />
                </svg>
                {t("ActionInstancesSelector.instances")}
                <Tag className={styles.Count}>
                    {selectedCount === totalInstances ? t("ActionInstancesSelector.all") : selectedCount}
                </Tag>
                <svg className={styles.Arrow} viewBox="0 0 24 24" aria-hidden="true">
                    <path d="m5 15 7-7 7 7" />
                </svg>
            </Button>
        </Popover>
    );
};
