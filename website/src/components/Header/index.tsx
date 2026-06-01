import { LogoIcon } from "@/assets";

import { ExternalLinks } from "./ExternalLinks";
import { NavLinks } from "./NavLinks";
import styles from "./styles.module.css";

export const Header = () => {
    return (
        <nav className={`MainContainer ${styles.MainWrapper}`}>
            <div className={styles.InnerWrapper}>
                <LogoIcon className={styles.LogoIcon} />
                <NavLinks />
                <ExternalLinks />
            </div>
        </nav>
    );
};
