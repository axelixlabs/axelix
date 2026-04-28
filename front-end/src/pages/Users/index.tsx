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

import { EmptyHandler, Loader } from "components";
import { fetchData, filterUsers } from "helpers";
import { type IUser, type IUsersFilters, StatefulRequest } from "models";
import { getUsers } from "services";

import { UsersFirstSection } from "./UsersFirstSection";
import { UsersTable } from "./UsersTable";

const Users = () => {
    const [usersData, setUsersData] = useState(StatefulRequest.loading<IUser[]>());
    const [search, setSearch] = useState<string>("");

    const [filters, setFilters] = useState<IUsersFilters>({
        roles: [],
        providers: [],
    });

    const fetchUsers = (): void => {
        setUsersData(StatefulRequest.loading<IUser[]>())
        fetchData(setUsersData, () => getUsers());
    };

    useEffect(() => {
        fetchUsers()
    }, []);

    if (usersData.loading) {
        return <Loader />;
    }

    if (usersData.error) {
        return <EmptyHandler isEmpty />;
    }

    const usersFeed = usersData.response!;
    const effectiveUsers = filterUsers(usersFeed, search, filters);
    const addonAfter = `${effectiveUsers.length} / ${usersFeed.length}`;

    return (
        <>
            <UsersFirstSection
                addonAfter={addonAfter}
                filters={filters}
                setFilters={setFilters}
                setSearch={setSearch}
                fetchUsers={fetchUsers}
            />
            <UsersTable users={effectiveUsers} />
        </>
    );
};

export default Users;
