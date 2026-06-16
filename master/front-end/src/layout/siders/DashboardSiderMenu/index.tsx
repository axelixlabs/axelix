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

import type { MenuItem as AntdMenuItem, ISiderMenuItem } from "models";

import styles from "./styles.module.css";

const createMenuItems = (items: ISiderMenuItem[]): AntdMenuItem[] => {
    return items.map(({ path, label }) => ({
        key: path,
        label: <Link to={path}>{label}</Link>,
    }));
};

// TODO: Add icons in future
const getDashboardItems = (): AntdMenuItem[] => {
    const overviewItems: ISiderMenuItem[] = [
        {
            path: "/dashboard/overview",
            label: "Overview",
        },
    ];

    const technologiesItems: ISiderMenuItem[] = [
        {
            path: "/dashboard/java",
            label: "Java",
        },
        {
            path: "/dashboard/persistence",
            label: "Persistence",
        },
        {
            path: "/dashboard/spring-framework",
            label: "Spring Framework",
        },
    ];

    return [
        ...createMenuItems(overviewItems),
        {
            key: "technologies",
            label: "Technologies",
            children: createMenuItems(technologiesItems),
        },
    ];
};

export const DashboardSiderMenu = () => {
    const { pathname } = useLocation();

    return (
        <Menu
            defaultSelectedKeys={["/dashboard/overview"]}
            defaultOpenKeys={["technologies"]}
            selectedKeys={[pathname]}
            mode="inline"
            items={getDashboardItems()}
            className={styles.Menu}
        />
    );
};
