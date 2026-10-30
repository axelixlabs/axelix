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
import { Link, useLocation } from "react-router";

import { DependencyHealthIcon, LanguagesUsageIcon, PersistenceIcon, SpringPortfolioIcon } from "@/assets";
import type { ISiderMenuItem, TMenuItem } from "@/models";

const createMenuItems = (items: ISiderMenuItem[]): TMenuItem[] => {
    return items.map(({ path, icon, label }) => ({
        key: path,
        icon: icon,
        label: <Link to={path}>{label}</Link>,
    }));
};

const getDashboardItems = (): TMenuItem[] => {
    const items: ISiderMenuItem[] = [
        {
            path: "/dashboard/spring-portfolio",
            icon: <SpringPortfolioIcon />,
            label: "Spring Portfolio",
        },
        {
            path: "/dashboard/languages",
            icon: <LanguagesUsageIcon />,
            label: "Languages Usage",
        },
        {
            path: "/dashboard/persistence",
            icon: <PersistenceIcon />,
            label: "Persistence",
        },
        {
            path: "/dashboard/dependencies",
            icon: <DependencyHealthIcon />,
            label: "Dependency Health",
        },
    ];

    return createMenuItems(items);
};

export const DashboardSiderMenu = () => {
    const { pathname } = useLocation();

    return <Menu selectedKeys={[pathname]} mode="inline" items={getDashboardItems()} />;
};
