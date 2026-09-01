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
import { Avatar, Badge, Checkbox } from "antd";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";
import { Link } from "react-router";

import { UserRoleTags } from "@/components";
import { getInitials } from "@/helpers";
import type { IUser } from "@/models";

import sharedStyles from "../shared.module.css";

import styles from "./styles.module.css";

interface IProps {
    user: IUser;
}

export const UsersTableRow = ({ user }: IProps) => {
    const { t } = useTranslation();
    const { id, username, email, roles, lastLoginAt, userOrigin } = user;

    const formattedLastLogin = lastLoginAt ? dayjs(lastLoginAt).format("DD.MM.YYYY HH:mm") : t("Users.notLoggedIn");
    const userDetailsPath = `/users/${id}`;

    return (
        <>
            <div className={`TableRow ${sharedStyles.TableRow}`}>
                <div className="TableRowChunk">
                    <Checkbox />
                </div>

                <Link
                    to={userDetailsPath}
                    state={{
                        user: user,
                    }}
                    className={styles.RowLink}
                >
                    <div className="TableRowChunk">
                        <div className={styles.UserDataWrapper}>
                            <Avatar>{getInitials(username)}</Avatar>
                            <div>
                                <div className={styles.PrimaryText}>{username}</div>
                                <div className={`TextSmall ${styles.SecondaryText}`}>{email}</div>
                            </div>
                        </div>
                    </div>
                    <div className="TableRowChunk">
                        <div className={styles.PrimaryText}>Operations</div>
                        <div className={`TextSmall ${styles.SecondaryText}`}>Realibility Engineer</div>
                    </div>
                    <div className="TableRowChunk">
                        <Badge status="success" text="Active" />
                    </div>
                    <div className="TableRowChunk">
                        <UserRoleTags roles={roles} />
                    </div>
                    <div className="TableRowChunk">{userOrigin}</div>
                    <div className="TableRowChunk">{formattedLastLogin}</div>
                </Link>
            </div>
        </>
    );
};
