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
import type { EProvider, ERoles } from "../enums/users";

export interface IUser {
    /**
     * Unique identifier of the user
     */
    id: string;

    /**
     * Email address of the user, which may be null
     */
    email: string | null;

    /**
     * Last login data of the most recent successful login. null if the user has never logged in
     */
    lastLoginAt: string | null;

    /**
     * Origin of the user account
     */
    provider: EProvider;

    /**
     * The roles granted to this user
     */
    roles: ERoles[];

    /**
     * Login name of the user
     */
    username: string;
}

export interface ICreateUserFormFields {
    /**
     * Login name of the user
     */
    username: string;

    /**
     * Email address of the user
     */
    email: string | undefined;

    /**
     * Password of the user
     */
    password: string;

    /**
     * The roles granted to this user
     */
    role: ERoles;
}

export interface ICreateUserRequestData extends Omit<ICreateUserFormFields, "email"> {
    /**
     * Email address of the user, which may be null
     */
    email: string | null;
}

export interface IEditUserRequestData {
    /**
     * Unique identifier of the user
     */
    id: string;

    /**
     * Login name of the user
     */
    username: string;

    /**
     * Email address of the user, which may be null
     */
    email: string | null;

    /**
     * Password of the user
     */
    password: string | null;

    /**
     * The roles granted to this user
     */
    roles: ERoles[];
}

export interface IUsersFilters {
    /**
     * The roles granted to this user.
     */
    roles: ERoles[];

    /**
     * Origins of users account
     */
    providers: EProvider[];
}

export interface IEditableUser extends Omit<IUser, "email"> {
    /**
     * Email address of the user
     */
    email: string;

    /**
     * Password of the user
     */
    password: string;
}
