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
import dayjs from "dayjs";

import { EUserDisplayStatus, EUserStatus, type IUser, type IUsersFilters } from "@/models";

const DORMANT_AFTER_DAYS = 90;

/**
 * An active user is dormant once their last login is older than {@link DORMANT_AFTER_DAYS} days.
 * Users who have never logged in are not considered dormant.
 */
export const getUserDisplayStatus = (user: IUser): EUserDisplayStatus => {
    if (user.status === EUserStatus.SUSPENDED) {
        return EUserDisplayStatus.SUSPENDED;
    }

    if (user.lastLoginAt && dayjs().diff(dayjs(user.lastLoginAt), "day") >= DORMANT_AFTER_DAYS) {
        return EUserDisplayStatus.DORMANT;
    }

    return EUserDisplayStatus.ACTIVE;
};

/**
 * Returns null when neither the first nor the last name is known.
 */
export const getUserFullName = ({ firstName, lastName }: IUser): string | null => {
    const fullName = [firstName, lastName].filter(Boolean).join(" ");

    return fullName || null;
};

export const getUserInitials = (user: IUser): string => {
    const { firstName, lastName, username } = user;

    if (firstName || lastName) {
        return `${firstName?.[0] ?? ""}${lastName?.[0] ?? ""}`.toUpperCase();
    }

    return username[0].toUpperCase();
};

const matchesSearch = (user: IUser, search: string): boolean => {
    if (!search) {
        return true;
    }

    const searchable = [user.username, user.email, getUserFullName(user)];

    return searchable.some((value) => value?.toLowerCase().includes(search));
};

const matchesFilters = (user: IUser, { statuses, roles, userOrigins }: IUsersFilters): boolean => {
    if (statuses.length > 0 && !statuses.includes(getUserDisplayStatus(user))) {
        return false;
    }

    if (roles.length > 0 && !user.roles.some((role) => roles.includes(role))) {
        return false;
    }

    return userOrigins.length === 0 || userOrigins.includes(user.userOrigin);
};

export const filterUsers = (users: IUser[], search: string, filters: IUsersFilters): IUser[] => {
    const formattedSearch = search.toLowerCase().trim();

    return users.filter((user) => matchesFilters(user, filters) && matchesSearch(user, formattedSearch));
};

export const emptyStringToNull = (value: string): string | null => {
    if (value === "") {
        return null;
    }

    return value;
};
