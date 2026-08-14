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

const securityRows = [
    { label: "Password", value: "Managed by Okta" },
    { label: "Active sessions", value: "2 devices" },
    { label: "API tokens", value: "1 active" },
    { label: "Failed sign-ins (30d)", value: "1", valueClass: "Danger" },
];

export const SecurityCard = () => {
    return (
        <>
            <UserAccessCard title="Security">
                {securityRows.map(({ label, valueClass, value }, index) => (
                    <div className={styles.SecurityRow} key={index}>
                        <span>{label}</span>
                        <span className={`${valueClass ? styles[valueClass] : ""}`}>{value}</span>
                    </div>
                ))}
            </UserAccessCard>
        </>
    );
};
