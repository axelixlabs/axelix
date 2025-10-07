import { Layout } from "antd";
import { Outlet } from "react-router-dom";

import { AdminHeader } from "./AdminHeader";
import { SiderMenu } from "./SiderMenu";

import styles from "./styles.module.css";

const { Content, Sider } = Layout;

interface IProps {
  /**
   * If hideSider is true, the sider will not be displayed.
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
          <Content className={styles.Content}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
};
