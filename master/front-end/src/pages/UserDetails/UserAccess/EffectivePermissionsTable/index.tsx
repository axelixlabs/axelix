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
import { AdministrationTable, StyledTag } from "@/components";
import { ERoles } from "@/models";

import styles from "./styles.module.css";

const permissions = [
    { name: "Users - create, edit, delete", level: "Full", levelClass: "Full", role: ERoles.ADMIN },
    { name: "Roles & permissions", level: "Full", levelClass: "Full", role: ERoles.ADMIN },
    { name: "Dashboards", level: "Edit", levelClass: "Edit", role: ERoles.EDITOR },
    { name: "Wallboards", level: "Edit", levelClass: "Edit", role: ERoles.VIEWER },
    { name: "MCP servers", level: "Read only", levelClass: "ReadOnly", role: ERoles.EDITOR },
    { name: "Billing", level: "No access", levelClass: "NoAccess", role: "-" },
];

export const EffectivePermissionsTable = () => {
    return (
        <>
            <AdministrationTable title="Effective permissions">
                {permissions.map(({ name, levelClass, level, role }) => (
                    <div className={`TableRow ${styles.TableRow}`} key={name}>
                        <div className="TableRowChunk">{name}</div>
                        <div className={`TableRowChunk ${styles.PermissionLevel} ${styles[levelClass]}`}>{level}</div>
                        <div className="TableRowChunk">
                            <StyledTag>{role}</StyledTag>
                        </div>
                    </div>
                ))}
            </AdministrationTable>
        </>
    );
};
