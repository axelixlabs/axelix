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
import { type KeyboardEvent, useRef } from "react";
import { useTranslation } from "react-i18next";

import type { TInstanceOption } from "@/models";

import { InstanceRow } from "./InstanceRow";
import styles from "./styles.module.css";

interface IProps {
    /** Instances displayed in the selector */
    instances: TInstanceOption[];

    /** Identifier of the visually selected instance */
    selectedInstanceId: string;

    /** Selects an instance without changing the route */
    onSelect: (instance: TInstanceOption) => void;

    /** Accessible identifier of the menu */
    menuId: string;
}

export const InstanceList = ({ instances, selectedInstanceId, onSelect, menuId }: IProps) => {
    const { t } = useTranslation();
    const menuRef = useRef<HTMLDivElement>(null);

    const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>): void => {
        if (event.key !== "ArrowDown" && event.key !== "ArrowUp") {
            return;
        }

        event.preventDefault();
        const buttons = Array.from(menuRef.current!.querySelectorAll<HTMLButtonElement>("button"));
        const currentIndex = buttons.indexOf(document.activeElement as HTMLButtonElement);
        const offset = event.key === "ArrowDown" ? 1 : -1;
        buttons[(currentIndex + offset + buttons.length) % buttons.length]?.focus();
    };

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Heading}>{t("InstanceContext.instancesCount", { count: instances.length })}</div>
            <div
                ref={menuRef}
                id={menuId}
                role="menu"
                aria-label={t("InstanceContext.instances")}
                className={styles.Options}
                onKeyDown={handleKeyDown}
            >
                {instances.map((instance) => (
                    <InstanceRow
                        key={instance.instanceId}
                        instance={instance}
                        selected={instance.instanceId === selectedInstanceId}
                        onSelect={onSelect}
                    />
                ))}
            </div>
        </div>
    );
};
