import styles from "./styles.module.css"

export const IncidentCard = () => {
    return (
        <div className={styles.Incident}>
            <div className={styles.Ih}>
                <span className={styles.Lhs}>
                    <span className={styles.Dot}></span> incident  orders-api
                </span>
                <span className={styles.Sev}>P1  customer-facing</span>
                <span className={styles.Rhs}>tue 02:14  03:38</span>
            </div>

            <div className={styles.Tl}>
                <div className={`${styles.TlRow} ${styles.Hit}`}>
                    <div className={styles.T}>02:14</div>
                    <div className={styles.Event}>
                        <h4>
                            Grafana fires: p99 on <code>/checkout</code> jumps 8.
                        </h4>
                    </div>
                </div>

                <div className={styles.TlRow}>
                    <div className={styles.T}>02:21</div>
                    <div className={styles.Event}>
                        <h4>
                            On-call opens six tabs. None of them show <em>why</em>.
                        </h4>
                        <span className={styles.Blocked}>
                            blocked by: dashboards watch, they don&apos;t ask
                        </span>
                    </div>
                </div>

                <div className={styles.TlRow}>
                    <div className={styles.T}>02:34</div>
                    <div className={styles.Event}>
                        <h4>
                            Needs <code>org.hibernate.SQL</code> at <code>TRACE</code> to see the slow
                            query.
                        </h4>
                        <span className={styles.Blocked}>blocked by: redeploy required</span>
                    </div>
                </div>

                <div className={styles.TlRow}>
                    <div className={styles.T}>02:47</div>
                    <div className={styles.Event}>
                        <h4>Wants a thread dump while the lock is still held.</h4>
                        <span className={styles.Blocked}>blocked by: actuator port not exposed</span>
                    </div>
                </div>

                <div className={styles.TlRow}>
                    <div className={styles.T}>03:02</div>
                    <div className={styles.Event}>
                        <h4>SSH into the pod. Flips a logger by hand. Nothing audits it.</h4>
                        <span className={styles.Blocked}>blocked by: no surface, no trail</span>
                    </div>
                </div>

                <div className={styles.TlRow}>
                    <div className={styles.T}>03:24</div>
                    <div className={styles.Event}>
                        <h4>Logs finally land. A connection pool is exhausted by a retry storm.</h4>
                    </div>
                </div>

                <div className={`${styles.TlRow} ${styles.Hit}`}>
                    <div className={styles.T}>03:38</div>
                    <div className={styles.Event}>
                        <h4>
                            Incident closed. <em>Customers already saw it.</em>
                        </h4>
                        <span className={`${styles.Blocked} ${styles.Ok}`}>
                            root cause found  84 minutes after the alert
                        </span>
                    </div>
                </div>
            </div>

            <div className={styles.Iff}>
                <span>84 min  6 tools  1 SSH session  0 audit entries</span>
                <b>The JVM knew the answer the whole time.</b>
            </div>
        </div>
    )
}