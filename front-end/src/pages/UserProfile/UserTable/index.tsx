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
import { useTranslation } from "react-i18next";

import type { IEditableUser, IUser } from "models";

import { EditableValue } from "../EditableValue";
import { RolesSelect } from "../RolesSelect";

import styles from "./styles.module.css";

import { EmailIcon, LockOutlinedIcon, ProfileIcon, ShieldIcon } from "assets";

interface IProps {
    /**
     * The user data
     */
    user: IUser;

    /**
     * Callback for re-loading the given user.
     */
    reLoadUser: () => void;
}

export const UserTable = ({ user, reLoadUser }: IProps) => {
    const { t } = useTranslation();

    // Converts nullable IUser fields to strings for convenience in forms
    const editableUser: IEditableUser = {
        ...user,
        email: user.email ?? "",
        password: "",
    };

    return (
        <div className={`CustomizedTable ${styles.Table}`}>
            <div className="TableHeader">
                <div className="RowChunk">{t("Users.userInformation")}</div>
            </div>
            <div className="TableRow">
                <div className="RowChunk">
                    <ProfileIcon /> <span className={styles.Label}>{t("username")}</span>
                </div>
                <div className="RowChunk">
                    <EditableValue user={editableUser} field="username" reLoadUser={reLoadUser} />
                </div>
            </div>
            <div className="TableRow">
                <div className="RowChunk">
                    <EmailIcon /> <span className={styles.Label}>Email</span>
                </div>
                <div className="RowChunk">
                    <EditableValue user={editableUser} field="email" reLoadUser={reLoadUser} />
                </div>
            </div>
            <div className="TableRow">
                <div className="RowChunk">
                    <ShieldIcon /> <span className={styles.Label}>{t("Users.roles")}</span>
                </div>
                <div className="RowChunk">
                    <RolesSelect user={editableUser} reLoadUser={reLoadUser} />
                </div>
            </div>

            <div className="TableRow">
                <div className="RowChunk">
                    <LockOutlinedIcon /> <span className={styles.Label}>{t("password")}</span>
                </div>
                <div className="RowChunk">
                    <EditableValue user={editableUser} field="password" reLoadUser={reLoadUser} />
                </div>
            </div>
        </div>
    );
};
