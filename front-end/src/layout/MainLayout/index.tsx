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
