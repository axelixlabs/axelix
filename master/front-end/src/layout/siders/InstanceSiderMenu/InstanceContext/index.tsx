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
import { useLocation, useNavigate, useParams } from "react-router";

import { InstanceCommit, InstanceUptime } from "@/components/InstanceChips";
import { switchInstancePath } from "@/helpers";
import { useInstanceProfile, useInstanceSiblings } from "@/hooks";
import type { TInstanceOption } from "@/models";

import { InstanceList } from "./InstanceList";
import styles from "./styles.module.css";

export const InstanceContext = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { pathname } = useLocation();
    const navigate = useNavigate();
    const profile = useInstanceProfile();
    const { siblings, load } = useInstanceSiblings(instanceId!);
    const [open, setOpen] = useState<boolean>(false);
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

    if (profile.loading) {
        return (
            <div className={styles.MainWrapper}>
                <Skeleton active title paragraph={{ rows: 1 }} />
            </div>
        );
    }

    if (!profile.response) {
        return null;
    }

    const currentInstance = profile.response.instance;
    const { name, commitShaShort, deployedAt } = currentInstance;
    // Until the siblings arrive, the list shows just the current instance
    const instances = siblings.length > 0 ? siblings : [currentInstance];

    const handleOpenChange = (visible: boolean): void => {
        setOpen(visible);
        if (visible) {
            load();
        }
    };

    // The URL is what every instance page reads its instance from, so switching the instance means navigating to the
    // same section of the selected one. A new history entry, so that Back returns to the previous instance
    const selectInstance = (selected: TInstanceOption): void => {
        setOpen(false);

        if (selected.instanceId === instanceId) {
            triggerRef.current?.focus();
            return;
        }

        navigate(switchInstancePath(pathname, instanceId!, selected.instanceId));
    };

    return (
        <Popover
            trigger="click"
            placement="bottomLeft"
            arrow={false}
            open={open}
            onOpenChange={handleOpenChange}
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
                    selectedInstanceId={currentInstance.instanceId}
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
                        handleOpenChange(true);
                    }
                }}
            >
                <span className={styles.Name}>{name}</span>
                <svg className={styles.Arrow} viewBox="0 0 24 24" aria-hidden="true">
                    <path d="m5 15 7-7 7 7" />
                </svg>
                <span className={styles.Meta}>
                    <InstanceCommit commitShaShort={commitShaShort} />
                    <InstanceUptime deployedAt={deployedAt} className={styles.Time} />
                </span>
            </button>
        </Popover>
    );
};
