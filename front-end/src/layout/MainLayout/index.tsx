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
import { Layout } from "antd";
import { Outlet } from "react-router-dom";

import { AdminHeader } from "./AdminHeader";
import { SiderMenu } from "./SiderMenu";
import styles from "./styles.module.css";

const { Content, Sider } = Layout;

interface IProps {
    /**
     * When hideSider is true, sider will be hidden.
     */
    hideSider?: boolean;
}

export const MainLayout = ({ hideSider }: IProps) => {
    return (
        <>
            <Layout className={styles.MainWrapper}>
                <AdminHeader />

                <Sider width={270} className={`${styles.Sider} ${hideSider ? styles.HideSider : ""}`}>
                    <div className={styles.SiderScrollContainer}>
                        <SiderMenu />
                    </div>
                </Sider>

                <Layout className={styles.ContentLayout}>
                    <Content className={`${styles.Content} ${!hideSider ? styles.WithSider : ""}`}>
                        <Outlet />
                    </Content>
                </Layout>
            </Layout>
        </>
    );
};
