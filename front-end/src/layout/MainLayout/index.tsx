import { Layout } from "antd";
import { Outlet, useLocation } from "react-router-dom";

import { AdminHeader } from "./AdminHeader";
import { SiderMenu } from "./SiderMenu";

import styles from "./styles.module.css";

const { Content, Sider } = Layout;

export const MainLayout = () => {
  const location = useLocation();
  const hideSider = location.pathname === "/wallboard";

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
          <Content className={styles.Content}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
};
