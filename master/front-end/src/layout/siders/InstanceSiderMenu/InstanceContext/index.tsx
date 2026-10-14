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
import { Popover, Skeleton } from "antd";
import { useEffect, useId, useRef, useState } from "react";
import { useTranslation } from "react-i18next";

import { InstanceCommit } from "@/components/InstanceChips";
import { useInstancePreviewData } from "@/hooks";
import type { TInstanceOption } from "@/models";
import { DEMO_INSTANCES } from "@/utils/instances";

import { InstanceList } from "./InstanceList";
import styles from "./styles.module.css";

export const InstanceContext = () => {
    const { t } = useTranslation();
    const instance = useInstancePreviewData();
    const [open, setOpen] = useState<boolean>(false);
    const [selectedInstance, setSelectedInstance] = useState<TInstanceOption | null>(null);
    const triggerRef = useRef<HTMLButtonElement>(null);
    const menuId = useId();

    useEffect(() => {
        if (!open) {
            return;
        }

        const handleEscape = (event: globalThis.KeyboardEvent): void => {
            if (event.key === "Escape") {
                setOpen(false);
                triggerRef.current?.focus();
            }
        };

        document.addEventListener("keydown", handleEscape);
        return () => document.removeEventListener("keydown", handleEscape);
    }, [open]);

    if (instance.loading) {
        return (
            <div className={styles.MainWrapper}>
                <Skeleton active title paragraph={{ rows: 1 }} />
            </div>
        );
    }

    if (!instance.response) {
        return null;
    }

    const displayedInstance = selectedInstance ?? instance.response;
    const { name, commitShaShort, deployedFor } = displayedInstance;
    const instances = [instance.response, ...DEMO_INSTANCES];

    // TODO: Replace the visual-only selection with navigation when the backend provides
    // real instances grouped by application (groupId + artifactId). The list currently
    // contains mock instances, and page data still uses the instanceId from the URL.
    // Selecting a real instance must update the route so every instance page loads
    // its data, preserving the current section when available and falling back to Details.
    const selectInstance = (selected: TInstanceOption): void => {
        setSelectedInstance(selected);
        setOpen(false);
        triggerRef.current?.focus();
    };

    return (
        <Popover
            trigger="click"
            placement="bottomLeft"
            arrow={false}
            open={open}
            onOpenChange={setOpen}
            afterOpenChange={(visible) => {
                if (visible) {
                    document.getElementById(menuId)?.querySelector<HTMLButtonElement>('[aria-checked="true"]')?.focus();
                }
            }}
            destroyOnHidden
            align={{ offset: [0, 8] }}
            classNames={{ root: styles.Popup }}
            styles={{ container: { padding: 0, borderRadius: 10 } }}
            content={
                <InstanceList
                    instances={instances}
                    selectedInstanceId={displayedInstance.instanceId}
                    onSelect={selectInstance}
                    menuId={menuId}
                />
            }
        >
            <button
                ref={triggerRef}
                type="button"
                className={`${styles.MainWrapper} ${open ? styles.Pressed : ""}`}
                aria-label={t("InstanceContext.currentInstance", { name })}
                aria-haspopup="menu"
                aria-expanded={open}
                aria-controls={open ? menuId : undefined}
                onKeyDown={(event) => {
                    if (event.key === "ArrowDown") {
                        event.preventDefault();
                        setOpen(true);
                    }
                }}
            >
                <span className={styles.Name}>{name}</span>
                <svg className={styles.Arrow} viewBox="0 0 24 24" aria-hidden="true">
                    <path d="m5 15 7-7 7 7" />
                </svg>
                <span className={styles.Meta}>
                    <InstanceCommit commitShaShort={commitShaShort} />
                    <span className={styles.Time} title={t("InstanceContext.uptime")}>
                        <svg viewBox="0 0 16 16" aria-hidden="true">
                            <circle cx="8" cy="8" r="6" />
                            <path d="M8 4v4l3 2" />
                        </svg>
                        <span aria-label={t("InstanceContext.uptimeValue", { value: deployedFor })}>{deployedFor}</span>
                    </span>
                </span>
            </button>
        </Popover>
    );
};
