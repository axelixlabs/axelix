import { Logo } from "../Logo";

import { ExternalLinks } from "./ExternalLinks";
import { NavLinks } from "./NavLinks";
import styles from "./styles.module.css";

export const Header = () => {
    return (
        <nav className={`MainContainer ${styles.MainWrapper}`}>
            <div className={styles.InnerWrapper}>
                <Logo className={styles.Logo} />
                <NavLinks />
                <ExternalLinks />
            </div>
        </nav>
    );
};
