import styles from "./styles.module.css";

export const HeroBenefits = () => {
    return (
        <div className={styles.BenefitsWrapper}>
            <div>
                <div className={styles.Label}>Built for</div>
                <div className={styles.Value}>Production</div>
                <div className={styles.SmallValue}>not only sandbox envs</div>
            </div>
            <div>
                <div className={styles.Label}>Exposed to</div>
                <div className={styles.Value}>Humans &amp; agents</div>
                <div className={styles.SmallValue}>same RBAC, same audit</div>
            </div>
            <div>
                <div className={styles.Label}>Install</div>
                <div className={styles.Value}>Fast setup</div>
                <span className={styles.SmallValue}>docker · helm · Jar</span>
            </div>
        </div>
    );
};
