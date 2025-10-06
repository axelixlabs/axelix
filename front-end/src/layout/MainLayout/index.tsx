import { useTranslation } from "react-i18next";
import { Layout, Menu, type MenuProps } from "antd";
import { Outlet, useNavigate } from "react-router-dom";

import { AdminHeader } from "./AdminHeader";

import styles from "./styles.module.css";

const { Content, Sider } = Layout;

type MenuItem = Required<MenuProps>["items"][number];

interface IProps {
  /**
   * If hideSider is true, the sider will not be displayed.
  */
  hideSider?: boolean;
}

export const MainLayout = ({ hideSider }: IProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const items: MenuItem[] = [
    {
      key: "insights",
      label: t("insights"),
      children: [
        {
          key: "details",
          label: t("details"),
        },
        {
          key: "metrics",
          label: t("metrics"),
        },
        {
          key: "environment",
          label: t("environment"),
        },
        {
          key: "beans",
          label: "Beans",
        },
        {
          key: "config-props",
          label: t("configurationProperties"),
        },
        {
          key: "scheduled-tasks",
          label: t("scheduledTasks"),
        },
      ],
    },
    { key: "loggers", label: t("loggers") },
    { key: "jvm", label: "JVM" },
    { key: "mappings", label: t("mappings") },
    { key: "caches", label: t("caches") },
  ];

  return (
    <Layout className={styles.MainWrapper}>
      <AdminHeader />

      <Layout>
        {hideSider || <Sider width={270} className={styles.Sider}>
          <Menu
            mode="inline"
            items={items}
            onClick={({ key }) => navigate(key)}
            className={styles.Menu}
          />
        </Sider>}

        <Layout className={styles.ContentLayout}>
          <Content className={styles.Content}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
};
