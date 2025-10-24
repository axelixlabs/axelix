import { useTranslation } from "react-i18next";
import { NavLink } from "react-router-dom";

import styles from "./styles.module.css";

export const NavigationBar = () => {
    const { t } = useTranslation();

    return (
        <nav data-test="header-links">
            <NavLink
                to="/dashboard"
                className={({ isActive }) => (isActive ? `${styles.Link} ${styles.ActiveLink}` : styles.Link)}
            >
                {t("Header.dashboard")}
            </NavLink>
            <NavLink
                to="/wallboard"
                className={({ isActive }) => (isActive ? `${styles.Link} ${styles.ActiveLink}` : styles.Link)}
            >
                {t("Header.wallboard")}
            </NavLink>
        </nav>
    );
};
