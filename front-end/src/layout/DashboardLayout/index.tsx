import { Outlet } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Layout, Menu, type MenuProps } from "antd";

import { AdminHeader } from "./AdminHeader";

import styles from "./styles.module.css";

const { Content, Sider } = Layout;

type MenuItem = Required<MenuProps>["items"][number];

export const DashboardLayout = () => {
  const { t } = useTranslation();

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
          key: "configurationProperties",
          label: t("configurationProperties"),
        },
        {
          key: "scheduledTasks",
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
        <Sider width={270} className={styles.Sider}>
          <Menu
            mode="inline"
            items={items}
            className={styles.Menu}
            // todo fix this in future
            // onClick={onClick}
            // onOpenChange={onOpenChange}
            // selectedKeys={[pathnameMainPart]}
          />
        </Sider>

        <Layout style={{ padding: "24px" }}>
          <Content
            style={{
              padding: 24,
              margin: 0,
              minHeight: 280,
              background: "#fff",
            }}
          >
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
};
