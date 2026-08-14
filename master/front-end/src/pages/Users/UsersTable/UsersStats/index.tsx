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
import type { IUsersStats } from "models";

import styles from "./styles.module.css";

// TODO: Remove this mock data
const stats: IUsersStats[] = [
    { label: "Total users", value: "412", detail: "398 OIDC · 14 local" },
    { label: "Admins", value: "7", detail: "full administration" },
    { label: "Suspended", value: "6", detail: "sign-in blocked" },
    { label: "Dormant 90d+", value: "31", detail: "candidates to remove" },
];

export const UsersStats = () => {
    return (
        <div className={styles.MainWrapper}>
            {stats.map(({ label, value, detail }) => (
                <div className={styles.Cell} key={label}>
                    <div className={`TextUltraSmall ${styles.Label}`}>{label}</div>
                    <div className={styles.Row}>
                        <span className="TextMedium">{value}</span>
                        <span className={`TextSmall ${styles.Detail}`}>{detail}</span>
                    </div>
                </div>
            ))}
        </div>
    );
};
