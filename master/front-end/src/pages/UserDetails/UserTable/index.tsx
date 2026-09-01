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

import { EmailIcon, LockOutlinedIcon, ProfileIcon, ShieldIcon } from "@/assets";
import type { IEditableUser, IUser } from "@/models";

import { EditableValue } from "../EditableValue";

import { RolesSelect } from "./RolesSelect";
import styles from "./styles.module.css";

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
        <div className={`CustomTable ${styles.Table}`}>
            <div className="TableHeader">
                <div className="TableRowChunk">{t("Users.userInformation")}</div>
            </div>
            <div className="TableRow">
                <div className="TableRowChunk">
                    <ProfileIcon /> <span className={styles.Label}>{t("username")}</span>
                </div>
                <div className="TableRowChunk">
                    <EditableValue user={editableUser} field="username" reLoadUser={reLoadUser} />
                </div>
            </div>
            <div className="TableRow">
                <div className="TableRowChunk">
                    <EmailIcon /> <span className={styles.Label}>Email</span>
                </div>
                <div className="TableRowChunk">
                    <EditableValue user={editableUser} field="email" reLoadUser={reLoadUser} />
                </div>
            </div>
            <div className="TableRow">
                <div className="TableRowChunk">
                    <ShieldIcon /> <span className={styles.Label}>{t("Users.roles")}</span>
                </div>
                <div className="TableRowChunk">
                    <RolesSelect user={editableUser} reLoadUser={reLoadUser} />
                </div>
            </div>

            <div className="TableRow">
                <div className="TableRowChunk">
                    <LockOutlinedIcon /> <span className={styles.Label}>{t("password")}</span>
                </div>
                <div className="TableRowChunk">
                    <EditableValue user={editableUser} field="password" reLoadUser={reLoadUser} />
                </div>
            </div>
        </div>
    );
};
