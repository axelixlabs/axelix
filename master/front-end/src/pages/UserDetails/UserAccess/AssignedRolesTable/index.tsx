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
import { AdministrationTable, StyledTag, ManageIncludedRoles } from "@/components";
import { ERoles } from "@/models";

import styles from "./styles.module.css";

const assignedRoles = [
    { role: ERoles.ADMIN, description: "Assigned directly by M. Keller", date: "14 Mar 2025" },
    { role: ERoles.EDITOR, description: "Inherited from ADMIN", date: "14 Mar 2025" },
    { role: ERoles.VIEWER, description: 'From group "Operations · Hamburg"', date: "02 Jun 2026" },
];

export const AssignedRolesTable = () => {
    return (
        <>
            <AdministrationTable
                title="Assigned roles"
                headerSecondColumn={<ManageIncludedRoles />}
            >
                {assignedRoles.map(({ role, description, date }) => (
                    <div className={`TableRow ${styles.TableRow}`} key={description}>
                        <div className="TableRowChunk">
                            <StyledTag>{role}</StyledTag>
                        </div>
                        <div className="TableRowChunk">{description}</div>
                        <div className="TableRowChunk">{date}</div>
                    </div>
                ))}
            </AdministrationTable>
        </>
    );
};
