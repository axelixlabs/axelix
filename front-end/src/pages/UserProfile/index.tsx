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
import {Link, useLocation, useNavigate} from "react-router";

import {type IUser} from "models";

import {UserProfileActions} from "./UserProfileActions";
import {UserTable} from "./UserTable";
import styles from "./styles.module.css";

import {BackwardArrowIcon} from "assets";
import {useEffect, useState} from "react";
import {App} from "antd";

const UserProfile = () => {
    const {t} = useTranslation();
    const location = useLocation();
    const state = location.state;
    const navigate = useNavigate();
    const { message } = App.useApp();

    const [user, setUser] = useState<IUser | undefined>(state?.user);

    useEffect(() => {
        if (!user) {
            message.error(t("Users.userNotFound"));
            navigate("/users", { replace: true });
            return;
        }

        navigate(".", {
            replace: true,
            state: {
                user: user
            }
        });
    }, [user]);

    if (!user) {
        return null;
    }

    return (
        <>
            <Link to="/users" className={styles.BackwardWrapper}>
                <BackwardArrowIcon /> {t("Users.back")}
            </Link>

            <div className={`TextMedium ${styles.FirstSectionUsername}`}>{user.username}</div>

            <UserTable user={user} setUser={setUser}/>

            <UserProfileActions userId={user.id}/>
        </>
    );
};

export default UserProfile;
