import styles from "./styles.module.css";

export const EnterpriseStack = () => {
    return (
        <div className={styles.MainWrapper}>
            <div className={`${styles.Card} ${styles.FirstCard}`}>
                <div className={styles.CardMainContent}>
                    <div className={styles.Tier}>Tier 3 · Support</div>
                    <div className={styles.CardTitle}>Dedicated support &amp; SLA</div>
                    <div className={styles.Meta}>24/7 · named engineer · priority CVE</div>
                </div>
                <span className={`${styles.Badge} ${styles.FirstCardBadge}`}>contract</span>
            </div>
            <div className={`${styles.Card} ${styles.SecondCard}`}>
                <div className={styles.CardMainContent}>
                    <div className={styles.Tier}>Tier 2 · Extensions</div>
                    <div className={styles.CardTitle}>Enterprise extensions</div>
                    <div className={styles.Meta}>SSO · RBAC · audit · multi-cluster · on-prem</div>
                </div>
                <span className={styles.Badge}>licensed</span>
            </div>
            <div className={`${styles.Card} ${styles.ThirdCard}`}>
                <div className={styles.CardMainContent}>
                    <div className={styles.Tier}>Tier 1 · Foundation</div>
                    <div className={styles.CardTitle}>Axelix OSS</div>
                    <div className={styles.Meta}>LGPL-3.0 · forever free · the same console</div>
                </div>
                <span className={styles.Badge}>always open</span>
            </div>
        </div>
    );
};
