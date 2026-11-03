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
import { Button } from "antd";
import { useTranslation } from "react-i18next";

import { StyledTag } from "@/components";
import type { IUser } from "@/models";

import { ProfileCard } from "../ProfileCard";
import { compareRoles } from "../permissions";

import styles from "./styles.module.css";

interface IProps {
    /**
     * User whose roles are shown
     */
    user: IUser;

    /**
     * Opens the change-roles dialog
     */
    onChangeRoles: () => void;
}

export const ProfileRoles = ({ user, onChangeRoles }: IProps) => {
    const { t } = useTranslation();
    const heldRoles = [...user.roles].sort((left, right) => compareRoles(right, left));

    return (
        <ProfileCard
            title={t("Users.roles")}
            extra={
                <Button type="link" className={styles.ChangeRoles} onClick={onChangeRoles}>
                    {t("Users.Profile.changeRoles")}
                </Button>
            }
        >
            {heldRoles.map((role) => (
                <div key={role} className={styles.Role}>
                    <StyledTag>{role}</StyledTag>
                    <p>{t(`Users.Profile.RoleDescriptions.${role}`)}</p>
                </div>
            ))}
        </ProfileCard>
    );
};
