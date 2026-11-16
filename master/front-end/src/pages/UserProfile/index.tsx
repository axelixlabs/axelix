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
import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router";

import { EmptyHandler, Loader } from "@/components";
import { fetchData, getUserFullName } from "@/helpers";
import { EUserOrigin, type IUserProfile, StatefulRequest } from "@/models";
import { getUserProfile } from "@/services";

import { ChangeRoles } from "./ChangeRoles";
import { EditUser } from "./EditUser";
import { ProfileDetails } from "./ProfileDetails";
import { ProfileHeader } from "./ProfileHeader";
import { ProfilePermissions } from "./ProfilePermissions";
import { ProfileRoles } from "./ProfileRoles";
import { SuspendUser } from "./SuspendUser";
import styles from "./styles.module.css";

const UserProfile = () => {
    const { userId } = useParams();
    const [userResponse, setUserResponse] = useState(StatefulRequest.loading<IUserProfile>());
    const [editing, setEditing] = useState(false);
    const [changingRoles, setChangingRoles] = useState(false);
    const [suspending, setSuspending] = useState(false);

    const loadUser = useCallback((): void => {
        setUserResponse(StatefulRequest.loading<IUserProfile>());
        fetchData(setUserResponse, () => getUserProfile(userId!));
    }, [userId]);

    useEffect(() => {
        loadUser();
    }, [loadUser]);

    if (userResponse.loading) {
        return <Loader />;
    }

    if (userResponse.error || !userResponse.response) {
        return <EmptyHandler isEmpty />;
    }

    const { user, roles, authorities } = userResponse.response;
    const displayName = getUserFullName(user) ?? user.username;
    const givenName = user.firstName?.trim() || displayName;

    return (
        <>
            <ProfileHeader
                user={user}
                displayName={displayName}
                onEdit={() => setEditing(true)}
                onSuspend={() => setSuspending(true)}
            />
            <div className={styles.Columns}>
                <ProfileDetails user={user} />
                <div>
                    <ProfileRoles user={user} roles={roles} onChangeRoles={() => setChangingRoles(true)} />
                    <ProfilePermissions authorities={authorities} />
                </div>
            </div>
            {user.userOrigin === EUserOrigin.LOCAL && (
                <EditUser user={user} open={editing} onClose={() => setEditing(false)} onSaved={loadUser} />
            )}
            <ChangeRoles
                user={user}
                roles={roles}
                displayName={displayName}
                givenName={givenName}
                open={changingRoles}
                onClose={() => setChangingRoles(false)}
                onSaved={loadUser}
            />
            <SuspendUser
                user={user}
                displayName={displayName}
                open={suspending}
                onClose={() => setSuspending(false)}
                onSaved={loadUser}
            />
        </>
    );
};

export default UserProfile;
