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
import { ConfigProvider, Layout } from "antd";
import type { JSX } from "react";
import { Outlet, useParams } from "react-router";

import { AccessProvider } from "@/components";
import { InstancePreviewProvider } from "@/components/InstancePreviewProvider";

import { AdminHeader } from "./AdminHeader";
// License key entry is moving to the Enterprise distro; banner temporarily disabled.
// import { LicenseStatusAlert } from "./LicenseStatusAlert";
import styles from "./styles.module.css";

const { Content, Sider } = Layout;

interface IProps {
    /**
     * The content of the sider
     */
    siderContent?: JSX.Element;
}

export const MainLayout = ({ siderContent }: IProps) => {
    const { instanceId } = useParams();

    const layout = (
        <>
            <AccessProvider />
            <Layout className={styles.MainWrapper}>
                <AdminHeader />

                {siderContent && (
                    <Sider width={270} className={styles.Sider}>
                        <ConfigProvider
                            theme={{
                                components: {
                                    Menu: {
                                        itemBg: "transparent",
                                        subMenuItemBg: "transparent",
                                        activeBarBorderWidth: 0,
                                    },
                                },
                            }}
                        >
                            <div className={styles.SiderScrollContainer}>{siderContent}</div>
                        </ConfigProvider>
                    </Sider>
                )}

                <Layout className={styles.ContentLayout}>
                    <Content className={`${styles.Content} ${siderContent ? styles.WithSider : ""}`}>
                        {/* License key entry is moving to the Enterprise distro; banner temporarily disabled. */}
                        {/* <LicenseStatusAlert hideSider={hideSider} /> */}
                        <Outlet />
                    </Content>
                </Layout>
            </Layout>
        </>
    );

    return instanceId ? (
        <InstancePreviewProvider key={instanceId} instanceId={instanceId}>
            {layout}
        </InstancePreviewProvider>
    ) : (
        layout
    );
};
