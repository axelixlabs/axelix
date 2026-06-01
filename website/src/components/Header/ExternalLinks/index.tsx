import { GithubIcon } from "@/assets";

import styles from "./styles.module.css";

export const ExternalLinks = () => {
    return (
        <div className={styles.ExternalLinksWrapper}>
            <a
                href="https://axelix.io/docs/product/introduction"
                className={styles.ExternalLink}
                target="_blank"
                rel="noopener noreferrer"
            >
                Docs
            </a>
            <a
                href="https://github.com/axelixlabs/axelix"
                target="_blank"
                rel="noopener noreferrer"
                className={styles.GitHubIcon}
            >
                <GithubIcon width="11" height="11" />
                GitHub
            </a>
        </div>
    );
};
