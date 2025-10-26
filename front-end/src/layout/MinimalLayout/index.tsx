import { Outlet } from "react-router-dom";

import { LanguageSwitcher } from "components";
import LogoIcon from "src/assets/icons/logo.png";

import styles from "./styles.module.css";

export const MinimalLayout = () => {
    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Header}>
                <div className="MainContainer">
                    <div className={styles.LanguageSwitcherWrapper}>
                        <img src={LogoIcon} alt="Axile logo" className={styles.Logo} />
                        <LanguageSwitcher />
                    </div>
                </div>
            </div>
            <div className={styles.ContentWrapper}>
                <Outlet />
            </div>
        </div>
    );
};
