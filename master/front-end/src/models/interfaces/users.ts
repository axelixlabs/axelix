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
import type { ERoles, EUserDisplayStatus, EUserOrigin, EUserStatus } from "../enums/users";

export interface IUser {
    /**
     * Unique identifier of the user
     */
    id: string;

    /**
     * First name of the user, which may be null, e.g. when the OIDC provider does not return the claim
     */
    firstName: string | null;

    /**
     * Last name of the user, which may be null, e.g. when the OIDC provider does not return the claim
     */
    lastName: string | null;

    /**
     * Email address of the user, which may be null
     */
    email: string | null;

    /**
     * Status that controls whether the user can log in
     */
    status: EUserStatus;

    /**
     * Last login data of the most recent successful login. null if the user has never logged in
     */
    lastLoginAt: string | null;

    /**
     * Origin of the user account
     */
    userOrigin: EUserOrigin;

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
     * First name of the user
     */
    firstName: string | undefined;

    /**
     * Last name of the user
     */
    lastName: string | undefined;

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
     * Repeated password, must match the password
     */
    confirmPassword: string;

    /**
     * The roles granted to this user
     */
    roles: ERoles[];
}

export interface ICreateUserRequestData {
    /**
     * Login name of the user
     */
    username: string;

    /**
     * First name of the user, which may be null
     */
    firstName: string | null;

    /**
     * Last name of the user, which may be null
     */
    lastName: string | null;

    /**
     * Email address of the user, which may be null
     */
    email: string | null;

    /**
     * Password of the user
     */
    password: string;

    /**
     * The roles granted to this user
     */
    roles: ERoles[];
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
     * Statuses of the user, as shown in the users list.
     */
    statuses: EUserDisplayStatus[];

    /**
     * The roles granted to this user.
     */
    roles: ERoles[];

    /**
     * Origins of users account
     */
    userOrigins: EUserOrigin[];
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
