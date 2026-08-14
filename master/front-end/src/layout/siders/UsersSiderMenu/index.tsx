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
import { Menu } from "antd";
import { useTranslation } from "react-i18next";
import { Link, useLocation } from "react-router";

import type { MenuItem as AntdMenuItem, ISiderMenuItem } from "models";

const createMenuItems = (items: ISiderMenuItem[]): AntdMenuItem[] => {
    return items.map(({ path, label }) => ({
        key: path,
        label: <Link to={path}>{label}</Link>,
    }));
};

export const UsersSiderMenu = () => {
    const { t } = useTranslation();
    const { pathname } = useLocation();

    // TODO: Add icons in future
    const getUsersItems = (): AntdMenuItem[] => {
        const administrationItems: ISiderMenuItem[] = [
            {
                path: "/users",
                label: t("Users.SiderMenu.users"),
            },
            {
                path: "/users/roles-permissions",
                label: t("Users.SiderMenu.rolesPermissions"),
            },
            {
                path: "/users/authentication",
                label: t("Users.SiderMenu.authentication"),
            },
            {
                path: "/users/audit-log",
                label: t("Users.SiderMenu.auditLog"),
            },
        ];

        return createMenuItems(administrationItems);
    };

    return <Menu defaultSelectedKeys={["/users"]} selectedKeys={[pathname]} mode="inline" items={getUsersItems()} />;
};
