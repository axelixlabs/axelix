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
import { useLocation, useNavigate, useParams } from "react-router-dom";

import { findOpenKeys } from "helpers";
import { getItems } from "utils";

import styles from "./styles.module.css";

export const SiderMenu = () => {
    const { t } = useTranslation();

    const navigate = useNavigate();
    const location = useLocation();
    const { instanceId } = useParams();

    return (
        <Menu
            mode="inline"
            items={getItems(instanceId!, t)}
            onClick={({ key }) => navigate(key)}
            selectedKeys={[location.pathname]}
            defaultOpenKeys={findOpenKeys(getItems(instanceId!, t))}
            className={styles.Menu}
        />
    );
};
