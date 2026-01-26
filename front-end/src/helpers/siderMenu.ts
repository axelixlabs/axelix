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
import type { MenuItem } from "models";

export const findOpenKeys = (items: MenuItem[], pathname: string): string[] => {
    const openKeys: string[] = [];
    let hasJvmChildMatch = false;

    for (const item of items) {
        if (!item?.key) {
            continue;
        }

        if (item.key !== "JVM") {
            openKeys.push(String(item.key));
            continue;
        }

        if (!("children" in item) || !Array.isArray(item.children)) {
            continue;
        }

        hasJvmChildMatch = item.children.some((child) => String(child?.key) === pathname);
    }

    if (hasJvmChildMatch) {
        openKeys.push("JVM");
    }

    return openKeys;
};
