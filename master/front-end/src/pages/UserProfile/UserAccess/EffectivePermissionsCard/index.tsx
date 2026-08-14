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
import { ERoles } from "models";
import { UserAccessCard } from "../UserAccessCard";

import styles from "./styles.module.css";

const effectivePermissions = [
    { name: "Users — create, edit, delete", level: "Full", levelClass: "Full", role: ERoles.ADMIN },
    { name: "Roles & permissions", level: "Full", levelClass: "Full", role: ERoles.ADMIN },
    { name: "Dashboards", level: "Edit", levelClass: "Edit", role: ERoles.EDITOR },
    { name: "Wallboards", level: "Edit", levelClass: "Edit", role: ERoles.VIEWER },
    { name: "MCP servers", level: "Read only", levelClass: "ReadOnly", role: ERoles.EDITOR },
    { name: "Billing", level: "No access", levelClass: "NoAccess", role: "—" },
];

export const EffectivePermissionsCard = () => {
    return (
        <>
            <UserAccessCard title="Effective permissions">
                {effectivePermissions.map(({ name, levelClass, level, role }, index) => (
                    <div className={styles.PermissionRow} key={index}>
                        <span className={styles.PermissionName}>{name}</span>
                        <span className={`${styles.PermissionLevel} ${styles[levelClass]}`}>{level}</span>
                        <span className={styles.PermissionRole}>{role}</span>
                    </div>
                ))}
            </UserAccessCard>
        </>
    );
};
