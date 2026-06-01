import styles from "./styles.module.css";

export const AccessBadge = () => {
    return (
        <div className={styles.MainWrapper}>
            <span className={`${styles.Label} ${styles.Web}`}>Web</span>
            <span className={styles.Separator}>·</span>
            <span className={`${styles.Label} ${styles.Mcp}`}>MCP</span>
        </div>
    );
};
