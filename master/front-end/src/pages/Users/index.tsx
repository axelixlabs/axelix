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
import { CreateUser } from "./UsersFirstSection/CreateUser";
import { UsersStats } from "./UsersTable/UsersStats";
import { Pagination } from "antd";
import { useEffect, useState } from "react";

import { EmptyHandler, Loader, PagesFirstSection } from "components";
import { fetchData, filterUsers } from "helpers";
import { useAppSelector } from "hooks";
import { type IUser, type IUsersFilters, StatefulRequest } from "models";
import { getUsers } from "services";
import { LOCAL_AUTH_OPTION_TYPE_NAME, PAGINATION_SIZE } from "utils";

import { UsersFirstSection } from "./UsersFirstSection";
import { UsersTable } from "./UsersTable";
import styles from "./styles.module.css";

const Users = () => {
    const settings = useAppSelector((state) => state.settings);

    const [usersData, setUsersData] = useState(StatefulRequest.loading<IUser[]>());
    const [search, setSearch] = useState<string>("");
    const [currentPage, setCurrentPage] = useState<number>(1);

    const [filters, setFilters] = useState<IUsersFilters>({
        roles: [],
        userOrigins: [],
    });

    const fetchUsers = (): void => {
        setUsersData(StatefulRequest.loading<IUser[]>());
        fetchData(setUsersData, () => getUsers());
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    useEffect(() => {
        setCurrentPage(1);
    }, [search, filters]);

    if (usersData.loading) {
        return <Loader />;
    }

    if (usersData.error) {
        return <EmptyHandler isEmpty />;
    }

    const usersFeed = usersData.response!;
    const effectiveUsers = filterUsers(usersFeed, search, filters);
    const addonAfter = `${effectiveUsers.length} / ${usersFeed.length}`;

    const pageStartIndex = (currentPage - 1) * PAGINATION_SIZE;
    const pageEndIndex = currentPage * PAGINATION_SIZE;

    const pageUsers = effectiveUsers.slice(pageStartIndex, pageEndIndex);

    const isLocalAuthEnabled = settings.authenticationOptions.some(({ type }) => type === LOCAL_AUTH_OPTION_TYPE_NAME);

    return (
        <>
            <div className={styles.PageFirstSectionWrapper}>
                <PagesFirstSection
                    title="Users"
                    subtitle="412 users in Northwind Industrial · sign-in via OIDC Provider and local password"
                />

                {isLocalAuthEnabled && <CreateUser fetchUsers={fetchUsers} />}
            </div>

            <UsersStats />

            <UsersFirstSection
                addonAfter={addonAfter}
                filters={filters}
                setFilters={setFilters}
                setSearch={setSearch}
            />

            <UsersTable users={pageUsers} />

            <Pagination
                current={currentPage}
                pageSize={PAGINATION_SIZE}
                total={effectiveUsers.length}
                onChange={setCurrentPage}
                className={styles.Pagination}
            />
        </>
    );
};

export default Users;
