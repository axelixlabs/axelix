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
import { DEMO_APP_URL } from "@/utils";

import styles from "./styles.module.css";

export const ServiceStackCard = () => {
    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Header}>
                <span className={styles.Lhs}>
                    <span className={styles.Dot} /> service · orders-service
                </span>
                <span className={styles.Tag}>reference app</span>
            </div>

            <div className={styles.Stack}>
                <div className={styles.Row}>
                    <span className={styles.Key}>Language</span>
                    <span className={styles.Value}>Java 25</span>
                </div>
                <div className={styles.Row}>
                    <span className={styles.Key}>Framework</span>
                    <span className={styles.Value}>Spring Boot 3.5</span>
                </div>
                <div className={styles.Row}>
                    <span className={styles.Key}>Persistence</span>
                    <span className={styles.Value}>Spring Data JPA · Hibernate</span>
                </div>
                <div className={styles.Row}>
                    <span className={styles.Key}>Database</span>
                    <span className={styles.Value}>PostgreSQL</span>
                </div>
                <div className={styles.Row}>
                    <span className={styles.Key}>Messaging</span>
                    <span className={styles.Value}>Apache Kafka</span>
                </div>
                <div className={styles.Row}>
                    <span className={styles.Key}>Build</span>
                    <span className={styles.Value}>Gradle</span>
                </div>
            </div>

            <div className={styles.Footer}>
                <span>The most common Spring Boot stack you&apos;ll ever meet.</span>
                <a href={DEMO_APP_URL} target="_blank" rel="noopener noreferrer" className={styles.SourceLink}>
                    View source →
                </a>
            </div>
        </div>
    );
};
