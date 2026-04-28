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
import type {IUser, IUsersFilters} from "models";

export const filterUsers = (users: IUser[], search: string, filters: IUsersFilters): IUser[] => {
    const formattedSearch = search.toLowerCase().trim();
    const hasRolesFilters = filters.roles.length > 0;
    const hasProvidersFilters = filters.providers.length > 0;

    return users.filter(({ username, email, roles, provider }) => {
        const matchesRoles = roles.some((role) => filters.roles.includes(role));
        if (hasRolesFilters && !matchesRoles) {
            return false;
        }

        const matchesProviders = filters.providers.includes(provider);
        if (hasProvidersFilters && !matchesProviders) {
            return false;
        }

        if (!formattedSearch) {
            return true;
        }

        const lowerUsername = username.toLowerCase();
        const lowerEmail = email.toLowerCase();
        const matchesUsername = lowerUsername.includes(formattedSearch);
        const matchesEmail = lowerEmail.includes(formattedSearch);

        return matchesUsername || matchesEmail;
    });
};