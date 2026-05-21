import { SparklesIcon } from "@/assets"
import styles from "./styles.module.css"

export const HeroActions = () => {
    return (
        <div className={styles.ActionsWrapper}>
            <a
                href="#install"
                className={`${styles.ActionButton} ${styles.GreenButton}`}
            >
                Get started free <span className={styles.Arrow}>→</span>
            </a>
            <a href="#enterprise" className={styles.ActionButton}>
                <SparklesIcon width="16" height="16" />
                Explore Enterprise
                <span className={styles.Badge}>In development</span>
            </a>
        </div>
    )
}