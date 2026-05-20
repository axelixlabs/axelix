import styles from "./styles.module.css";

export const HeroBenefits = () => {
    return (
        <div className={styles.Trust}>
            <div>
                <div className={styles.K}>Built for</div>
                <div className={styles.V}>
                    Production
                    <span className={styles.Small}>not test stands</span>
                </div>
            </div>
            <div>
                <div className={styles.K}>Exposed to</div>
                <div className={styles.V}>
                    Humans &amp; agents
                    <span className={styles.Small}>same RBAC, same audit</span>
                </div>
            </div>
            <div>
                <div className={styles.K}>Install</div>
                <div className={styles.V}>
                    Fast setup<span className={styles.Small}>docker · helm</span>
                </div>
            </div>
        </div>
    );
};
