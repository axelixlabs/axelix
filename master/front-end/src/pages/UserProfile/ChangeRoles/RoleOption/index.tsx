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

import type { ERoles } from "@/models";

import styles from "./styles.module.css";

type TRoleState = "current" | "adding" | "revoking" | "plain";

const roleState = (held: boolean, checked: boolean): TRoleState => {
    if (checked && held) {
        return "current";
    }

    if (checked) {
        return "adding";
    }

    if (held) {
        return "revoking";
    }

    return "plain";
};

interface IProps {
    /**
     * Role this row toggles
     */
    role: ERoles;

    /**
     * Whether the user currently holds the role
     */
    held: boolean;

    /**
     * Whether the role is selected in the dialog
     */
    checked: boolean;

    /**
     * Toggles the role in the pending selection
     */
    onToggle: (role: ERoles) => void;
}

export const RoleOption = ({ role, held, checked, onToggle }: IProps) => {
    const { t } = useTranslation();
    const state = roleState(held, checked);

    return (
        <div
            className={`${styles.Role} ${state === "adding" ? styles.Adding : ""} ${
                state === "revoking" ? styles.Revoking : ""
            }`}
            onClick={() => onToggle(role)}
        >
            <span onClick={(event) => event.stopPropagation()}>
                <Checkbox checked={checked} onChange={() => onToggle(role)} />
            </span>
            <span className={styles.RoleText}>
                <span className={styles.RoleHeader}>
                    <span className={styles.RoleName}>{role}</span>
                    {state === "current" && <span className={styles.Current}>{t("Users.Profile.current")}</span>}
                    {state === "adding" && <span className={styles.AddingLabel}>{t("Users.Profile.adding")}</span>}
                    {state === "revoking" && (
                        <span className={styles.RevokingLabel}>{t("Users.Profile.revoking")}</span>
                    )}
                </span>
                <span className={styles.RoleDescription}>{t(`Users.Profile.RoleDescriptions.${role}`)}</span>
            </span>
        </div>
    );
};
