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

import { EmptyHandler, Loader } from "@/components";
import { fetchData, filterUsers } from "@/helpers";
import { type IUser, type IUsersFilters, StatefulRequest } from "@/models";
import { getUsers } from "@/services";

import { UsersFirstSection } from "./UsersFirstSection";
import { UsersTable } from "./UsersTable";

const PAGE_SIZE = 10;

const Users = () => {
    const { t } = useTranslation();

    const [usersData, setUsersData] = useState(StatefulRequest.loading<IUser[]>());
    const [search, setSearch] = useState<string>("");
    const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
    const [page, setPage] = useState<number>(1);

    const [filters, setFilters] = useState<IUsersFilters>({
        statuses: [],
        roles: [],
        userOrigins: [],
    });

    useEffect(() => {
        setPage(1);
    }, [search, filters]);

    const fetchUsers = (): void => {
        setUsersData(StatefulRequest.loading<IUser[]>());
        fetchData(setUsersData, () => getUsers());
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    if (usersData.loading) {
        return <Loader />;
    }

    if (usersData.error) {
        return <EmptyHandler isEmpty />;
    }

    const usersFeed = usersData.response!;
    const effectiveUsers = filterUsers(usersFeed, search, filters);
    const pageStart = (page - 1) * PAGE_SIZE;
    const pageUsers = effectiveUsers.slice(pageStart, pageStart + PAGE_SIZE);
    const selectedUsers = usersFeed.filter((user) => selectedIds.has(user.id));

    const rangeLabel = t("Users.rangeOf", {
        from: pageUsers.length > 0 ? pageStart + 1 : 0,
        to: pageStart + pageUsers.length,
        total: effectiveUsers.length,
    });

    return (
        <>
            <UsersFirstSection
                users={usersFeed}
                rangeLabel={rangeLabel}
                filters={filters}
                setFilters={setFilters}
                setSearch={setSearch}
                fetchUsers={fetchUsers}
            />
            <UsersTable
                users={pageUsers}
                total={effectiveUsers.length}
                page={page}
                pageSize={PAGE_SIZE}
                onPageChange={setPage}
                selectedUsers={selectedUsers}
                setSelectedIds={setSelectedIds}
            />
        </>
    );
};

export default Users;
