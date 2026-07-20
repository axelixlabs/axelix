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
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router";

import { Loader } from "components";
import { type IUser, StatefulRequest } from "models";
import { getUserById } from "services";

import { UserProfileActions } from "./UserProfileActions";
import { UserTable } from "./UserTable";
import styles from "./styles.module.css";

import { BackwardArrowIcon } from "assets";

const UserProfile = () => {
    const { t } = useTranslation();
    const { userId } = useParams();

    const [userResponse, setUserResponse] = useState(StatefulRequest.loading<IUser>());

    const loadUser = () => {
        getUserById(userId!).then((value) => {
            setUserResponse(StatefulRequest.success(value.data));
        });
    };

    useEffect(() => loadUser(), [userId]);

    if (userResponse.loading) {
        return <Loader />;
    }

    const user = userResponse.response!;

    return (
        <>
            <Link to="/users" className={styles.BackwardWrapper}>
                <BackwardArrowIcon /> {t("Users.back")}
            </Link>

            <div className={`TextMedium ${styles.FirstSectionUsername}`}>{user.username}</div>

            <UserTable user={user} reLoadUser={loadUser} />

            <UserProfileActions userId={userId!} userOrigin={user.userOrigin} />
        </>
    );
};

export default UserProfile;
