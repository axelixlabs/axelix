import { Menu } from "antd";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate, useParams } from "react-router-dom";

import { findOpenKeys } from "helpers";
import { getItems } from "utils";

import styles from "./styles.module.css";

export const SiderMenu = () => {
    const { t } = useTranslation();

    const navigate = useNavigate();
    const location = useLocation();
    const { instanceId } = useParams();

    return (
        <Menu
            mode="inline"
            items={getItems(instanceId!, t)}
            onClick={({ key }) => navigate(key)}
            selectedKeys={[location.pathname]}
            defaultOpenKeys={findOpenKeys(getItems(instanceId!, t), location.pathname)}
            className={styles.Menu}
        />
    );
};
