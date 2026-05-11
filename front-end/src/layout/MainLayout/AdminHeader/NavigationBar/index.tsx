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
import { useTranslation } from "react-i18next";
import { useSelector } from "react-redux";
import { NavLink } from "react-router";

import type { RootState } from "store";

import styles from "./styles.module.css";

export const NavigationBar = () => {
    const { t } = useTranslation();
    const settings = useSelector((state: RootState) => state.settings);

    return (
        <>
            <nav data-test="header-links">
                {settings.isMcpServerEnabled && (
                    <NavLink
                        to="/mcp-server"
                        className={({ isActive }) => `${styles.Link} ${isActive ? styles.ActiveLink : ""}`}
                    >
                        MCP
                    </NavLink>
                )}
                <NavLink
                    to="/dashboard"
                    className={({ isActive }) => `${styles.Link} ${isActive ? styles.ActiveLink : ""}`}
                >
                    {t("Header.dashboard")}
                </NavLink>
                <NavLink
                    to="/wallboard"
                    className={({ isActive }) => `${styles.Link} ${isActive ? styles.ActiveLink : ""}`}
                >
                    {t("Header.wallboard")}
                </NavLink>
            </nav>
        </>
    );
};
