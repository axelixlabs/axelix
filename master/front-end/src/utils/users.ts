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
import type { InputProps } from "antd";
import type { TFunction } from "i18next";

import { ERoles, type IRoleCheckboxOption, type UserDetailsEditableValueField } from "@/models";

export const userDetailsInputTypeMap: Record<UserDetailsEditableValueField, InputProps["type"]> = {
    username: "text",
    email: "email",
    password: "password",
};

export const roleOptions = Object.values(ERoles).map((role) => {
    return {
        value: role,
    };
});

export const getRoleCheckboxOptions = (t: TFunction): IRoleCheckboxOption[] => {
    return [
        {
            value: ERoles.VIEWER,
            label: t("Users.CreateUser.Roles.Viewer.label"),
            description: t("Users.CreateUser.Roles.Viewer.description"),
        },
        {
            value: ERoles.EDITOR,
            label: t("Users.CreateUser.Roles.Editor.label"),
            description: t("Users.CreateUser.Roles.Editor.description"),
        },
        {
            value: ERoles.ADMIN,
            label: t("Users.CreateUser.Roles.Admin.label"),
            description: t("Users.CreateUser.Roles.Admin.description"),
        },
    ];
};
