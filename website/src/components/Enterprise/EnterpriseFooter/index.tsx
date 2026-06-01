import styles from "./styles.module.css";

export const EnterpriseFooter = () => {
    return (
        <div className={styles.MainWrapper}>
            <div>
                <span className={styles.Eyebrow}>Early access · Q3 2026</span>
                <h3 className={styles.Title}>Want a seat in the first pilots?</h3>
                <p className={styles.Description}>
                    We&apos;re picking a handful of teams to shape what ships first. Tell us about your fleet —
                    we&apos;ll be in touch within a working day.
                </p>
            </div>
            <a href="mailto:enterprise@axelix.io" className={styles.Button}>
                enterprise@axelix.io <span className={styles.Arrow}>→</span>
            </a>
        </div>
    );
};
