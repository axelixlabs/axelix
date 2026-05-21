import styles from "./styles.module.css"

export const InstallerFooter = () => {
    return (
        <div className={styles.Meta}>
            <span className={styles.Chip}>Spring Boot 2 · 3 · 4</span>
            <span className={styles.Chip}>JVM 11 — 25</span>
            <span className={styles.Chip}>Docker · Helm</span>
        </div>
    )
} 