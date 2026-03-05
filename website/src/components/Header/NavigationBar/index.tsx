import styles from "./styles.module.css";

export const NavigationBar = () => {
    return (
        <nav>
            <ul className={styles.LinksWrapper}>
                <li>
                    <a href="https://axelix.io/docs/introduction" target="_blank" rel="noopener noreferrer" className={styles.Link}>Docs</a>
                </li>
                <li>
                    <a href="https://axelix.io/blog" target="_blank" rel="noopener noreferrer" className={styles.Link}>Blog</a>
                </li>
                <li>
                    <a href="#faq-title" className={styles.Link}>FAQ</a>
                </li>
            </ul>
        </nav>
    );
};
