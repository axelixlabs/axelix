import { useTranslation } from "react-i18next";
import { NavLink } from "react-router-dom";

import styles from "./styles.module.css";

export const NavigationBar = () => {
    const { t } = useTranslation();

    return (
        <nav data-test="header-links">
            <NavLink
                to="/dashboard"
                className={({ isActive }) => `${styles.Link} ${isActive ? styles.ActiveLink : ""}`}
            >
                {t("Header.dashboard")}
            </NavLink>
            <NavLink
                to="/wallboard"
                className={({ isActive }) => `${styles.Link} ${isActive ? styles.ActiveLink : ""}`}
            >
                {t("Header.wallboard")}
            </NavLink>
        </nav>
    );
};
