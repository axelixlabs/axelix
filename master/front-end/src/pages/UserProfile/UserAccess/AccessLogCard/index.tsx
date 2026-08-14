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
import { UserAccessCard } from "../UserAccessCard";

import styles from "./styles.module.css";

const ACCESS_LOG_SUCCESS = "Success";
const ACCESS_LOG_FAILED = "Failed";

const accessLog = [
    { title: "Signed in via SSO (Okta)", meta: "04 Aug 2026 09:12 · 10.14.2.88", status: ACCESS_LOG_SUCCESS },
    { title: "Changed role on user l.fischer", meta: "03 Aug 2026 18:40 · 10.14.2.88", status: ACCESS_LOG_SUCCESS },
    { title: "Signed in via SSO (Okta)", meta: "03 Aug 2026 08:02 · 10.14.2.88", status: ACCESS_LOG_SUCCESS },
    { title: "Sign-in attempt rejected by Okta", meta: "01 Aug 2026 22:15 · 84.117.9.201", status: ACCESS_LOG_FAILED },
    { title: 'Created API token "grafana-read"', meta: "01 Aug 2026 10:31 · 10.14.2.88", status: ACCESS_LOG_SUCCESS },
];

export const AccessLogCard = () => {
    return (
        <>
            <UserAccessCard title="Access Log">
                {accessLog.map(({ title, meta, status }, index) => (
                    <div className={styles.LogRow} key={index}>
                        <div>
                            <div className={styles.LogTitle}>{title}</div>
                            <div className={`TextUltraSmall ${styles.LogMeta}`}>{meta}</div>
                        </div>
                        <span
                            className={`${styles.LogStatus} ${status === ACCESS_LOG_FAILED ? styles.Danger : styles.Success}`}
                        >
                            {status}
                        </span>
                    </div>
                ))}
            </UserAccessCard>
        </>
    );
};
