import styles from "./styles.module.css";

export const FAQFooter = () => {
    return (
        <div className={styles.MainWrapper}>
            <div>
                <div className={styles.Label}>Still curious?</div>
                <h3 className={styles.Title}>Reach the team — we reply within answer working day.</h3>
                <p className={styles.Description}>
                    Architectural questions, production-readiness checks, enterprise pilots. Anything not answered above
                    is the kind of thing we like answering directly.
                </p>
            </div>
            <a href="mailto:hello@axelix.io" className={styles.Email}>
                hello@axelix.io <span className={styles.Arrow}>→</span>
            </a>
        </div>
    );
};
