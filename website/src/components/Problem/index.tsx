import { IncidentCard } from "./IncidentCard";
import { ProblemTopSection } from "./ProblemTopSection";
import styles from "./styles.module.css";

export const Problem = () => {
      return (
        <section className={styles.Problem} id="problem">
            <div className={`wrap ${styles.Wrap}`}>
                <ProblemTopSection />

                <div className={styles.Body}>
                    <IncidentCard />

                    <aside className={styles.Takeaways}>
                        <div className={styles.Takeaway}>
                            <span className={styles.N}>01  The observability gap</span>
                            <h3>You can see a spike. You can&apos;t change a logger.</h3>
                            <p>
                                Metrics tell you something is wrong. They never let you reach into the JVM and adjust
                                anything live.
                            </p>
                        </div>
                        <div className={styles.Takeaway}>
                            <span className={styles.N}>02  The access gap</span>
                            <h3>Actuator is in every Spring Boot app  and exposed in none.</h3>
                            <p>
                                The introspection is already there. It&apos;s locked behind a port nobody trusts to
                                leave open.
                            </p>
                        </div>
                        <div className={styles.Takeaway}>
                            <span className={styles.N}>03  The agent gap</span>
                            <h3>Your on-call AI can read the dashboard. It can&apos;t act on it.</h3>
                            <p>
                                Copilots ingest screenshots. They can&apos;t flip a logger, pull a dump, or rotate a
                                pool.
                            </p>
                        </div>
                    </aside>
                </div>

                <p className={styles.Closer}>
                    Every other tool was built to <em>watch</em> production. <b>Axelix was built to operate it</b> 
                    from a browser, or from an AI agent, with the same auth, the same audit trail.
                </p>
            </div>
        </section>
    );
};

