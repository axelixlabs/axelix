import { useTranslation } from "react-i18next";
import { Link, useLocation } from "react-router-dom";

import styles from "./styles.module.css";

export const NavigationBar = () => {
    const { t } = useTranslation();
    const { pathname } = useLocation();

    const isWallboardPage = pathname.startsWith("/wallboard") || pathname === "/";
    const isDashboardPage = pathname.startsWith("/instance");

    return (
        <nav data-test="header-links">
            <Link to="/dashboard" className={`${styles.Link} ${isDashboardPage ? styles.ActiveLink : ""}`}>
                {t("Header.dashboard")}
            </Link>
            <Link to="/wallboard" className={`${styles.Link} ${isWallboardPage ? styles.ActiveLink : ""}`}>
                {t("Header.wallboard")}
            </Link>
        </nav>
    );
};
