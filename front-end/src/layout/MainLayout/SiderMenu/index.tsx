import { Menu } from "antd";
import { useTranslation } from "react-i18next";
import { useNavigate, useLocation, useParams } from "react-router-dom";

import { findOpenKeys } from "helpers";
import { getItems } from "utils";

import styles from "./styles.module.css";

export const SiderMenu = () => {
    const { t } = useTranslation();

    const navigate = useNavigate();
    const location = useLocation();
    const { id } = useParams();

    return (
        <Menu
            mode="inline"
            items={getItems(id, t)}
            onClick={({ key }) => navigate(key)}
            selectedKeys={[location.pathname]}
            defaultOpenKeys={findOpenKeys(getItems(id, t), location.pathname)}
            className={styles.Menu}
        />
    );
};
