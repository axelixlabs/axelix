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
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";
import { Link } from "react-router";

import { EmptyHandler } from "components";
import type { IUser } from "models";

import styles from "./styles.module.css";

export interface IProps {
    /**
     * List of users
     */
    users: IUser[];
}

export const UsersTable = ({ users }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`CustomizedTable ${styles.Table}`}>
                <div className={`TableHeader TableRow ${styles.TableHeader}`}>
                    <div className="RowChunk">{t("username")}</div>
                    <div className="RowChunk">Email</div>
                    <div className="RowChunk">{t("Users.lastLogin")}</div>
                    <div className="RowChunk">{t("Users.provider")}</div>
                    <div className="RowChunk">{t("Users.roles")}</div>
                </div>

                <EmptyHandler isEmpty={users.length === 0}>
                    {users.map((user) => {
                        const { id, username, email, roles, lastLoginAt, provider } = user;
                        const parsedRoles = roles.join(", ");
                        const formattedDate = dayjs(lastLoginAt).format("DD.MM.YYYY HH:mm");

                        return (
                            <Link to={id} state={{ user }} className={`TableRow ${styles.TableRow}`} key={id}>
                                <div className="RowChunk">{username}</div>
                                <div className="RowChunk">{email}</div>
                                <div className="RowChunk">{formattedDate}</div>
                                <div className="RowChunk">{provider}</div>
                                <div className="RowChunk">{parsedRoles}</div>
                            </Link>
                        );
                    })}
                </EmptyHandler>
            </div>
        </>
    );
};
