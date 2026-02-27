import Link from "next/link";

import styles from "./styles.module.css";

export const NavigationBar = () => {
    return (
        <nav>
            <ul className={styles.LinksWrapper}>
                <li>
                    <Link href="https://docs.axelix.io" className={styles.Link}>Docs</Link>
                </li>
                <li>
                    <Link href="https://blog.axelix.io" className={styles.Link}>Blog</Link>
                </li>
                <li>
                    <a href="#faq-title" className={styles.Link}>FAQ</a>
                </li>
            </ul>
        </nav>
    );
};
