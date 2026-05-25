import styles from "./styles.module.css"

export const ProblemRightCards = () => {
    return (
        <aside className={styles.MainWrapper}>
            <div className={styles.Card}>
                <div className={styles.CardTitle}>01  The observability gap</div>
                <h3 className={styles.CardSubtitle}>You can see a spike. You can&apos;t change a logger.</h3>
                <p className={styles.CardDescription}>
                    Metrics tell you something is wrong. They never let you reach into the JVM and adjust
                    anything live.
                </p>
            </div>
            <div className={styles.Card}>
                <div className={styles.CardTitle}>02  The access gap</div>
                <h3 className={styles.CardSubtitle}>Actuator is in every Spring Boot app  and exposed in none.</h3>
                <p className={styles.CardDescription}>
                    The introspection is already there. It&apos;s locked behind a port nobody trusts to
                    leave open.
                </p>
            </div>
            <div className={styles.Card}>
                <div className={styles.CardTitle}>03  The agent gap</div>
                <h3 className={styles.CardSubtitle}>Your on-call AI can read the dashboard. It can&apos;t act on it.</h3>
                <p className={styles.CardDescription}>
                    Copilots ingest screenshots. They can&apos;t flip a logger, pull a dump, or rotate a
                    pool.
                </p>
            </div>
        </aside>
    )
}