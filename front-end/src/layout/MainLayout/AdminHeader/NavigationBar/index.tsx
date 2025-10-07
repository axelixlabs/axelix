import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import styles from './styles.module.css'

export const NavigationBar = () => {
    const { t } = useTranslation()

    return (
        <nav data-test="header-links">
            <NavLink
                to="/dashboard"
                className={({ isActive }) =>
                    isActive ? `${styles.Link} ${styles.ActiveLink}` : styles.Link
                }
            >
                {t("dashboard")}
            </NavLink>
            <NavLink
                to="/wallboard"
                className={({ isActive }) =>
                    isActive ? `${styles.Link} ${styles.ActiveLink}` : styles.Link
                }
            >
                {t("wallboard")}
            </NavLink>
        </nav>
    )
};