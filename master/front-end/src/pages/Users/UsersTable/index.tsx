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
import { Checkbox, Pagination } from "antd";
import { useTranslation } from "react-i18next";

import { EmptyHandler } from "@/components";
import type { IUser } from "@/models";

import { SelectionBar } from "./SelectionBar";
import { UserRow } from "./UserRow";
import styles from "./styles.module.css";

interface IProps {
    /**
     * Users shown on the current page
     */
    users: IUser[];

    /**
     * Number of users matching the search and filters, across all pages
     */
    total: number;

    /**
     * Current page, starting from 1
     */
    page: number;

    /**
     * Number of users per page
     */
    pageSize: number;

    /**
     * Called when the page changes
     */
    onPageChange: (page: number) => void;

    /**
     * Currently selected users, possibly from other pages
     */
    selectedUsers: IUser[];

    /**
     * Replaces the selection with the given user ids
     */
    setSelectedIds: (ids: Set<string>) => void;

    /**
     * Called when the selected users should be suspended
     */
    onSuspend?: (users: IUser[]) => void;

    /**
     * Called when the selected users should be deleted
     */
    onDelete?: (users: IUser[]) => void;
}

export const UsersTable = ({
    users,
    total,
    page,
    pageSize,
    onPageChange,
    selectedUsers,
    setSelectedIds,
    onSuspend,
    onDelete,
}: IProps) => {
    const { t } = useTranslation();

    const selectedIds = new Set(selectedUsers.map((user) => user.id));
    const selectedOnPage = users.filter((user) => selectedIds.has(user.id)).length;
    const allOnPageSelected = users.length > 0 && selectedOnPage === users.length;

    const toggleUser = (userId: string): void => {
        const next = new Set(selectedIds);

        if (!next.delete(userId)) {
            next.add(userId);
        }

        setSelectedIds(next);
    };

    const togglePage = (): void => {
        const next = new Set(selectedIds);
        users.forEach((user) => (allOnPageSelected ? next.delete(user.id) : next.add(user.id)));
        setSelectedIds(next);
    };

    return (
        <>
            <div className={styles.Table}>
                {selectedUsers.length > 0 && (
                    <SelectionBar
                        selectedUsers={selectedUsers}
                        onSuspend={onSuspend}
                        onDelete={onDelete}
                        onClear={() => setSelectedIds(new Set())}
                    />
                )}

                <div className={`TextCaps ${styles.Grid} ${styles.TableHeader}`}>
                    <Checkbox
                        checked={allOnPageSelected}
                        indeterminate={selectedOnPage > 0 && !allOnPageSelected}
                        onChange={togglePage}
                        aria-label={t("Users.selectAll")}
                    />
                    <div>{t("Users.user")}</div>
                    <div>{t("username")}</div>
                    <div>{t("Users.status")}</div>
                    <div>{t("Users.roles")}</div>
                    <div>{t("Users.origin")}</div>
                    <div>{t("Users.lastActivity")}</div>
                </div>

                <EmptyHandler isEmpty={users.length === 0}>
                    {users.map((user) => (
                        <UserRow key={user.id} user={user} selected={selectedIds.has(user.id)} onToggle={toggleUser} />
                    ))}
                </EmptyHandler>

                <div className={`TextSmall ${styles.Footer}`}>
                    <span>{t("Users.rowsPerPage", { count: pageSize })}</span>
                    <Pagination
                        size="small"
                        current={page}
                        pageSize={pageSize}
                        total={total}
                        showSizeChanger={false}
                        onChange={onPageChange}
                    />
                </div>
            </div>
        </>
    );
};
