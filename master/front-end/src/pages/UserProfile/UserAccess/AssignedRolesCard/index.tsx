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

const assignedRoles = [
    { role: ERoles.ADMIN, description: "Assigned directly by M. Keller", date: "14 Mar 2025" },
    { role: ERoles.EDITOR, description: "Inherited from ADMIN", date: "14 Mar 2025" },
    { role: ERoles.VIEWER, description: 'From group "Operations · Hamburg"', date: "02 Jun 2026" },
];

export const AssignedRolesCard = () => {
    return (
        <>
            <UserAccessCard title="Assigned roles" action={<a className={`TextSmall ${styles.ManageLink}`}>Manage</a>}>
                {assignedRoles.map(({ role, description, date }, index) => (
                    <div className={styles.RoleRow} key={index}>
                        <span className={styles.RoleTag}>{role}</span>
                        <span className={styles.RoleDescription}>{description}</span>
                        <span className={`TextSmall ${styles.RoleDate}`}>{date}</span>
                    </div>
                ))}
            </UserAccessCard>
        </>
    );
};
