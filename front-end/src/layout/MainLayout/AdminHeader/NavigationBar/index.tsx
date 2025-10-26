import { useTranslation } from "react-i18next";
import { Link, NavLink, useLocation } from "react-router-dom";

import styles from "./styles.module.css";

export const NavigationBar = () => {
    const { t } = useTranslation();
    const { pathname } = useLocation();

    const isActive = pathname === "/wallboard" || pathname === "/";

    return (
        <nav data-test="header-links">
            <NavLink
                to="/dashboard"
                className={({ isActive }) => `${styles.Link} ${isActive ? styles.ActiveLink : ""}`}
            >
                {t("Header.dashboard")}
            </NavLink>
            <Link to="/wallboard" className={`${styles.Link} ${isActive ? styles.ActiveLink : ""}`}>
                {t("Header.wallboard")}
            </Link>
        </nav>
    );
};
