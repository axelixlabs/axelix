/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        <Layout className={styles.MainWrapper}>
            <AdminHeader />

            <Layout>
                {hideSider || (
                    <Sider width={270} className={styles.Sider}>
                        <SiderMenu />
                    </Sider>
                )}

                <Layout className={styles.ContentLayout}>
                    <Content className={`${styles.Content} ${!hideSider ? styles.WithSider : ""}`}>
                        <Outlet />
                    </Content>
                </Layout>
            </Layout>
        </Layout>
    );
};
