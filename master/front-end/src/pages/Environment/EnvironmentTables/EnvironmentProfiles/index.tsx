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

import { EnvironmentProfilesData } from "./EnvironmentProfilesData";
import styles from "./styles.module.css";

interface IProps {
    /**
     * List of active profiles (both default and explicitly activated) inside the given Spring Boot application
     */
    profiles: string[];
}

export const EnvironmentProfiles = ({ profiles }: IProps) => {
    const { t } = useTranslation();

    const [open, setOpen] = useState<boolean>(false);

    return (
        <Popover
            trigger="click"
            placement="bottomLeft"
            onOpenChange={setOpen}
            styles={{ container: { padding: 0 } }}
            content={<EnvironmentProfilesData profiles={profiles} />}
        >
            <button type="button" className={`${styles.Trigger} ${open ? styles.TriggerOpen : ""}`}>
                <ProfileIcon className={styles.TriggerIcon} />
                {t("Environments.profilesCount", { value: profiles.length })}
                <span className={styles.Caret}>{open ? "▴" : "▾"}</span>
            </button>
        </Popover>
    );
};
