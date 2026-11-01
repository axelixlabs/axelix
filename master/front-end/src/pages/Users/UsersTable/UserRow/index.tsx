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
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router";

import { UserRoleTags } from "@/components";
import { getUserDisplayStatus, getUserFullName, getUserInitials } from "@/helpers";
import { EUserDisplayStatus, EUserOrigin, type IUser } from "@/models";

import tableStyles from "../styles.module.css";

import styles from "./styles.module.css";

const MISSING_VALUE = "—";

const statusClassNames: Record<EUserDisplayStatus, string> = {
    [EUserDisplayStatus.ACTIVE]: styles.StatusActive,
    [EUserDisplayStatus.DORMANT]: styles.StatusDormant,
    [EUserDisplayStatus.SUSPENDED]: styles.StatusSuspended,
};

interface IProps {
    /**
     * User shown in the row
     */
    user: IUser;

    /**
     * Whether the row is selected
     */
    selected: boolean;

    /**
     * Toggles the selection of the row
     */
    onToggle: (userId: string) => void;
}

export const UserRow = ({ user, selected, onToggle }: IProps) => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const { id, username, email, roles, lastLoginAt } = user;
    const profilePath = `/administration/users/${id}`;
    const fullName = getUserFullName(user);
    const status = getUserDisplayStatus(user);
    const isLocal = user.userOrigin === EUserOrigin.LOCAL;

    return (
        <>
            <div
                className={`${tableStyles.Grid} ${styles.Row} ${selected ? styles.Selected : ""}`}
                onClick={() => navigate(profilePath, { state: { user } })}
            >
                <div onClick={(e) => e.stopPropagation()}>
                    <Checkbox
                        checked={selected}
                        onChange={() => onToggle(id)}
                        aria-label={t("Users.selectUser", { username })}
                    />
                </div>

                <div className={styles.Person}>
                    <span className={`${styles.Avatar} ${fullName ? "" : styles.AvatarAnonymous}`}>
                        {getUserInitials(user)}
                    </span>
                    <div className={styles.PersonText}>
                        <Link
                            to={profilePath}
                            state={{ user }}
                            onClick={(e) => e.stopPropagation()}
                            className={`${styles.Name} ${fullName ? "" : styles.Missing}`}
                        >
                            {fullName ?? MISSING_VALUE}
                        </Link>
                        <div className={`TextUltraSmall ${styles.Email} ${email ? "" : styles.Missing}`}>
                            {email ?? MISSING_VALUE}
                        </div>
                    </div>
                </div>

                <div className={styles.Username}>{username}</div>

                <div className={`${styles.Status} ${statusClassNames[status]}`}>
                    <span className={styles.StatusDot} />
                    {t(`Users.Statuses.${status}`)}
                </div>

                <UserRoleTags roles={roles} />

                <div className={isLocal ? styles.LocalOrigin : ""}>
                    {isLocal ? t("Users.originLocal") : t("Users.originOidc")}
                </div>

                <div className={styles.LastActivity}>
                    {lastLoginAt ? dayjs(lastLoginAt).format("DD.MM.YYYY HH:mm") : t("Users.never")}
                </div>
            </div>
        </>
    );
};
