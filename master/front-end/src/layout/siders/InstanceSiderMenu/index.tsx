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
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useParams } from "react-router";

import { findOpenInstanceCategory } from "@/helpers";
import { useInstanceProfile } from "@/hooks";
import { getInstanceItems } from "@/utils";

import { InstanceContext } from "./InstanceContext";
import styles from "./styles.module.css";

export const InstanceSiderMenu = () => {
    const { t } = useTranslation();

    const { pathname } = useLocation();
    const { instanceId } = useParams();

    // null while unknown (loading or failed), so that nothing is greyed out without a reason
    const profile = useInstanceProfile();
    const features = profile.response?.features ?? null;

    const items = getInstanceItems(instanceId!, t, features);
    // Several categories may stay open at once; the sider scrolls when they no longer fit the viewport
    const [openCategories, setOpenCategories] = useState<string[]>(() => [findOpenInstanceCategory(items, pathname)]);

    // Reveal the current page when it changes from outside the sider, e.g. through a link on the page
    useEffect(() => {
        const current = findOpenInstanceCategory(items, pathname);
        setOpenCategories((open) => (open.includes(current) ? open : [...open, current]));
    }, [pathname]);

    return (
        <>
            {instanceId && <InstanceContext key={instanceId} />}
            <Menu
                className={styles.Menu}
                mode="inline"
                items={items}
                selectedKeys={[pathname]}
                openKeys={openCategories}
                onOpenChange={setOpenCategories}
            />
        </>
    );
};
