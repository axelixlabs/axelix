/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { IncidentCardHeader } from "./IncidentCardHeader";
import styles from "./styles.module.css";

export const IncidentCard = () => {
    return (
        <div className={styles.MainWrapper}>
            <IncidentCardHeader />

            <div className={styles.ContentWrapper}>
                <div className={styles.TimelineRow}>
                    <div className={styles.Time}>02:14</div>
                    <div className={styles.EventWrapper}>
                        <h4 className={styles.EventTitle}>
                            Grafana fires: p99 on <code className={styles.Tag}>/checkout</code> jumps 8.
                        </h4>
                    </div>
                </div>

                <div className={styles.TimelineRow}>
                    <div className={styles.Time}>02:21</div>
                    <div className={styles.EventWrapper}>
                        <h4 className={styles.EventTitle}>
                            On-call opens six tabs. None of them show <em className={styles.AccentedText}>why</em>.
                        </h4>
                        <span className={styles.EventText}>blocked by: dashboards watch, they don&apos;t ask</span>
                    </div>
                </div>

                <div className={styles.TimelineRow}>
                    <div className={styles.Time}>02:34</div>
                    <div className={styles.EventWrapper}>
                        <h4 className={styles.EventTitle}>
                            Needs <code className={styles.Tag}>org.hibernate.SQL</code> at{" "}
                            <code className={styles.Tag}>TRACE</code> to see the slow query.
                        </h4>
                        <span className={styles.EventText}>blocked by: redeploy required</span>
                    </div>
                </div>

                <div className={styles.TimelineRow}>
                    <div className={styles.Time}>02:47</div>
                    <div className={styles.EventWrapper}>
                        <h4 className={styles.EventTitle}>Wants a thread dump while the lock is still held.</h4>
                        <span className={styles.EventText}>blocked by: actuator port not exposed</span>
                    </div>
                </div>

                <div className={styles.TimelineRow}>
                    <div className={styles.Time}>03:02</div>
                    <div className={styles.EventWrapper}>
                        <h4 className={styles.EventTitle}>
                            SSH into the pod. Flips a logger by hand. Nothing audits it.
                        </h4>
                        <span className={styles.EventText}>blocked by: no surface, no trail</span>
                    </div>
                </div>

                <div className={styles.TimelineRow}>
                    <div className={styles.Time}>03:24</div>
                    <div className={styles.EventWrapper}>
                        <h4 className={styles.EventTitle}>
                            Logs finally land. A connection pool is exhausted by a retry storm.
                        </h4>
                    </div>
                </div>

                <div className={`${styles.TimelineRow} ${styles.Hit}`}>
                    <div className={styles.Time}>03:38</div>
                    <div className={styles.EventWrapper}>
                        <h4 className={styles.EventTitle}>
                            Incident closed. <em className={styles.AccentedText}>Customers already saw it.</em>
                        </h4>
                        <span className={`${styles.EventText} ${styles.EventTextSuccess}`}>
                            root cause found 84 minutes after the alert
                        </span>
                    </div>
                </div>
            </div>

            <div className={styles.Footer}>
                84 min 6 tools 1 SSH session 0 audit entries
                <b className={styles.BoldedText}>The JVM knew the answer the whole time.</b>
            </div>
        </div>
    );
};
