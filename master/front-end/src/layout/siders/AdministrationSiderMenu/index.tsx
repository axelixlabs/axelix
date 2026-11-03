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

import { ShieldIcon, UsersIcon } from "@/assets";
import { useAuthority } from "@/hooks";
import { EAuthorities, type TMenuItem } from "@/models";

const USERS_PATH = "/administration/users";
const ROLES_PATH = "/administration/roles";

export const AdministrationSiderMenu = () => {
    const { t } = useTranslation();
    const { pathname } = useLocation();
    const hasUsersViewAccess = useAuthority(EAuthorities.USERS_VIEW);

    const items: TMenuItem[] = [];

    if (hasUsersViewAccess) {
        items.push(
            {
                key: USERS_PATH,
                icon: <UsersIcon width={16} height={16} />,
                label: <Link to={USERS_PATH}>{t("Header.users")}</Link>,
            },
            {
                key: ROLES_PATH,
                icon: <ShieldIcon width={16} height={16} />,
                label: <Link to={ROLES_PATH}>{t("Header.roles")}</Link>,
            },
        );
    }

    // The user profile page (/administration/users/:userId) still belongs to the "Users" entry
    const selectedKeys = items.map((item) => item!.key as string).filter((key) => pathname.startsWith(key));

    return <Menu selectedKeys={selectedKeys} mode="inline" items={items} />;
};
