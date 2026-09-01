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

import styles from "./styles.module.css";

interface IPermissionRow {
    actionName: string;
    scope: string;
    noAccess?: boolean;
    source: string;
}

interface ICategoryData {
    title: string;
    rows: IPermissionRow[];
}

const cardData: ICategoryData[] = [
    {
        title: "DASHBOARDS",
        rows: [
            { actionName: "read", scope: "All dashboards", source: "VIEWER" },
            { actionName: "read values", scope: "All dashboards", source: "VIEWER" },
            { actionName: "modify", scope: "No access", noAccess: true, source: "-" },
        ],
    },
    {
        title: "WALLBOARDS",
        rows: [
            { actionName: "read", scope: "All wallboards", source: "-" },
            { actionName: "modify", scope: "All wallboards", source: "direct" },
        ],
    },
    {
        title: "MCP SERVERS",
        rows: [
            { actionName: "read", scope: "All servers", source: "direct" },
            { actionName: "read values", scope: "No access", noAccess: true, source: "-" },
        ],
    },
    {
        title: "USERS & ROLES",
        rows: [
            { actionName: "read users", scope: "All users", source: "direct" },
            { actionName: "modify users", scope: "No access", noAccess: true, source: "-" },
            { actionName: "read roles", scope: "All roles", source: "direct" },
        ],
    },
];

export const GrantedPermissionsTable = () => {
    return (
        <>
            <AdministrationTable
                title="Granted permissions"
                headerSecondColumn={
                    <div className={`TextUltraSmall ${styles.GrantedPermissions}`}>7 of 18 permissions granted</div>
                }
            >
                {cardData.map((category) => (
                    <div key={category.title}>
                        <div className={`TableRowChunk TextUltraSmall ${styles.PermissionCategoryTitle}`}>
                            {category.title}
                        </div>

                        {category.rows.map((row) => (
                            <div className={`TableRow ${styles.PermissionRow}`} key={row.actionName}>
                                <div className="TableRowChunk">{row.actionName}</div>
                                <div className={`TableRowChunk ${row.noAccess ? styles.NoAccessScope : undefined}`}>
                                    {row.scope}
                                </div>

                                <div className="TableRowChunk">
                                    <StyledTag>{row.noAccess ? "-" : row.source}</StyledTag>
                                </div>
                            </div>
                        ))}
                    </div>
                ))}
            </AdministrationTable>
        </>
    );
};
