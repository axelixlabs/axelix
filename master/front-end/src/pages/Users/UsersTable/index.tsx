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

import { EmptyHandler } from "@/components";
import { ERoles, EUserOrigin, type IUser } from "@/models";

import { UsersTableRow } from "./UsersTableRow";
import sharedStyles from "./shared.module.css";

interface IProps {
    /**
     * List of users
     */
    users: IUser[];
}

export const UsersTable = ({ users }: IProps) => {
    const { t } = useTranslation();

    users = [
        {
            id: "1",
            username: "Dana Rowe",
            email: "dana.rowe@gmail.com",
            lastLoginAt: "string",
            userOrigin: EUserOrigin.LOCAL,
            roles: [ERoles.ADMIN, ERoles.EDITOR, ERoles.VIEWER],
        },
        {
            id: "2",
            email: "string",
            lastLoginAt: "string",
            userOrigin: EUserOrigin.LOCAL,
            roles: [ERoles.ADMIN],
            username: "Dana Rowe",
        },
    ];

    return (
        <>
            <div className="CustomTable">
                <div className={`TableHeader ${sharedStyles.TableRow}`}>
                    <div className="TableRowChunk">
                        <Checkbox />
                    </div>
                    <div className="TableRowChunk">{t("Users.Table.user")}</div>
                    <div className="TableRowChunk">{t("Users.Table.department")}</div>
                    <div className="TableRowChunk">{t("status")}</div>
                    <div className="TableRowChunk">{t("Users.Table.roles")}</div>
                    <div className="TableRowChunk">{t("Users.origin")}</div>
                    <div className="TableRowChunk">{t("Users.Table.lastActivity")}</div>
                </div>

                <EmptyHandler isEmpty={users.length === 0}>
                    {users.map((user) => {
                        return <UsersTableRow user={user} key={user.id} />;
                    })}
                </EmptyHandler>
            </div>
        </>
    );
};
