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
import {useTranslation} from "react-i18next";

import type {IUser} from "models";

import {EditableValue} from "../EditableValue";
import {RolesSelect} from "../RolesSelect";

import styles from "./styles.module.css";

import {EmailIcon, LockOutlinedIcon, ProfileIcon, ShieldIcon} from "assets";
import type {Dispatch, SetStateAction} from "react";

interface IProps {
    /**
     * The user data
     */
    user: IUser;

    /**
     * The setter of user data
     */
    setUser: Dispatch<SetStateAction<IUser>>;
}


export const UserTable = ({user, setUser}: IProps) => {
    const {t} = useTranslation();

    return (
        <div className={`CustomizedTable ${styles.Table}`}>
            <div className="TableHeader">
                <div className="RowChunk">{t("Users.userInformation")}</div>
            </div>
            <div className="TableRow">
                <div className="RowChunk">
                    <ProfileIcon/> <span className={styles.Label}>{t("username")}</span>
                </div>
                <div className="RowChunk">
                    <EditableValue user={user} field="username" setUser={setUser}/>
                </div>
            </div>
            <div className="TableRow">
                <div className="RowChunk">
                    <EmailIcon/> <span className={styles.Label}>Email</span>
                </div>
                <div className="RowChunk">
                    <EditableValue user={user} field="email" setUser={setUser}/>
                </div>
            </div>
            <div className="TableRow">
                <div className="RowChunk">
                    <ShieldIcon/> <span className={styles.Label}>{t("Users.roles")}</span>
                </div>
                <div className="RowChunk">
                    <RolesSelect user={user} setUser={setUser} />
                </div>
            </div>

            <div className="TableRow">
                <div className="RowChunk">
                    <LockOutlinedIcon/> <span className={styles.Label}>{t("password")}</span>
                </div>
                <div className="RowChunk">
                    <EditableValue user={user} field="password" setUser={setUser}/>
                </div>
            </div>
        </div>
    );
};
