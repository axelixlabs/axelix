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
import { Outlet } from "react-router-dom";

import { LanguageSwitcher } from "components";

import styles from "./styles.module.css";

import { LogoIcon } from "assets";

export const MinimalLayout = () => {
    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Header}>
                <div className="MainContainer">
                    <div className={styles.LanguageSwitcherWrapper}>
                        <LogoIcon className={styles.Logo} />
                        <LanguageSwitcher />
                    </div>
                </div>
            </div>
            <div className={styles.ContentWrapper}>
                <Outlet />
            </div>
        </div>
    );
};
