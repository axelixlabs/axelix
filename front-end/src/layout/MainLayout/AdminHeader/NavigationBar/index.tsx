import { useTranslation } from "react-i18next";
import { Link, useLocation } from "react-router-dom";

import { doesLocationStartWith } from "helpers";

import styles from "./styles.module.css";

export const NavigationBar = () => {
    const { t } = useTranslation();
    const { pathname } = useLocation();

    const isWallboardPage = doesLocationStartWith("/wallboard", pathname) || pathname === "/";

    return (
        <nav data-test="header-links">
            <Link
                to="/dashboard"
                className={`${styles.Link} ${doesLocationStartWith("/instance", pathname) ? styles.ActiveLink : ""}`}
            >
                {t("Header.dashboard")}
            </Link>
            <Link to="/wallboard" className={`${styles.Link} ${isWallboardPage ? styles.ActiveLink : ""}`}>
                {t("Header.wallboard")}
            </Link>
        </nav>
    );
};
