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
import { Popover } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { ProfileIcon } from "@/assets";

import styles from "./styles.module.css";

interface IProps {
    /**
     * List of active profiles inside the given Spring Boot application
     */
    activeProfiles: string[];
}

export const EnvironmentProfiles = ({ activeProfiles }: IProps) => {
    const { t } = useTranslation();

    const [open, setOpen] = useState<boolean>(false);

    const content = (
        <div className={styles.Dropdown}>
            <div className={styles.DropdownHeader}>
                <div className={styles.DropdownTitle}>{t("Environments.activeProfiles")}</div>
                <div className={styles.DropdownHint}>{t("Environments.profilesPrecedenceHint")}</div>
            </div>

            {activeProfiles.map((activeProfile, index) => {
                const isLastProfile = index === activeProfiles.length - 1;

                return (
                    <div
                        className={`${styles.ProfileRow} ${isLastProfile ? styles.HighestPrecedenceProfile : ""}`}
                        key={activeProfile}
                    >
                        <span className={styles.ProfileOrder}>{index + 1}</span>
                        <span className={styles.ProfileName}>{activeProfile}</span>
                    </div>
                );
            })}
        </div>
    );

    return (
        <Popover
            content={content}
            trigger="click"
            placement="bottomLeft"
            onOpenChange={setOpen}
            styles={{ container: { padding: 0 } }}
        >
            <button type="button" className={`${styles.Trigger} ${open ? styles.TriggerOpen : ""}`}>
                <ProfileIcon className={styles.TriggerIcon} />
                {t("Environments.profilesCount", { value: activeProfiles.length })}
                <span className={styles.Caret}>{open ? "▴" : "▾"}</span>
            </button>
        </Popover>
    );
};
