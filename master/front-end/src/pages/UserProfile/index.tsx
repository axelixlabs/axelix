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
import { Tabs } from "antd";
import { useEffect, useState } from "react";
import { useParams } from "react-router";

import { ERoles, EUserOrigin, type IUser, StatefulRequest } from "models";
import { getUserById } from "services";

import { UserAccess } from "./UserAccess";
import { UserProfileActions } from "./UserProfileActions";
import { UserProfileFirstSection } from "./UserProfileFirstSection";
import styles from "./styles.module.css";

const UserProfile = () => {
    const { userId } = useParams();

    const [userResponse, setUserResponse] = useState(StatefulRequest.loading<IUser>());

    const loadUser = () => {
        getUserById(userId!).then((value) => {
            setUserResponse(StatefulRequest.success(value.data));
        });
    };

    useEffect(() => loadUser(), [userId]);

    // if (userResponse.loading) {
    //     return <Loader />;
    // }

    // const user = userResponse.response!;
    const user = {
        id: "1",
        email: "string",
        lastLoginAt: "string",
        userOrigin: EUserOrigin.LOCAL,
        roles: [ERoles.ADMIN, ERoles.EDITOR, ERoles.VIEWER],
        username: "string",
    };

    const tabItems = [
        {
            key: "profile",
            label: "Profile",
            children: null,
        },
        {
            key: "access",
            label: "Access",
            children: <UserAccess />,
        },
        {
            key: "activity",
            label: "Activity",
            children: null,
        },
        {
            key: "sessions",
            label: "Sessions & tokens",
            children: null,
        },
    ];

    return (
        <>
            <UserProfileFirstSection username={user.username} />

            <Tabs defaultActiveKey="access" items={tabItems} className={styles.Tabs} />

            <UserProfileActions userId={userId!} userOrigin={user.userOrigin} />
        </>
    );
};

export default UserProfile;

// <UserTable user={user} reLoadUser={loadUser} />
