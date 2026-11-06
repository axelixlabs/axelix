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
import type { IRoleProfile, IUser } from "@/models";

import { ProfileCard } from "../ProfileCard";

import styles from "./styles.module.css";

interface IProps {
    /**
     * User whose roles are shown
     */
    user: IUser;

    /**
     * Roles from the roles feed. Null until the feed has loaded.
     */
    roles: IRoleProfile[] | null;

    /**
     * Opens the change-roles dialog
     */
    onChangeRoles: () => void;
}

const heldInFeedOrder = (held: readonly string[], roles: readonly IRoleProfile[]): string[] => {
    const order = new Map(roles.map((role, index) => [role.name, index]));

    return [...held].sort(
        (left, right) => (order.get(left) ?? Number.MAX_SAFE_INTEGER) - (order.get(right) ?? Number.MAX_SAFE_INTEGER),
    );
};

export const ProfileRoles = ({ user, roles, onChangeRoles }: IProps) => {
    const { t } = useTranslation();
    const descriptions = new Map(roles?.map((role) => [role.name, role.description]));
    const heldRoles = roles ? heldInFeedOrder(user.roles, roles) : user.roles;

    return (
        <ProfileCard
            title={t("Users.roles")}
            extra={
                <Button type="link" className={styles.ChangeRoles} disabled={roles === null} onClick={onChangeRoles}>
                    {t("Users.Profile.changeRoles")}
                </Button>
            }
        >
            {heldRoles.map((role) => {
                const description = descriptions.get(role);

                return (
                    <div key={role} className={styles.Role}>
                        <StyledTag>{role}</StyledTag>
                        {description && <p>{description}</p>}
                    </div>
                );
            })}
        </ProfileCard>
    );
};
