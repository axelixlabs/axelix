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
import { useState } from "react";
import { Link } from "react-router";

import { EmptyHandler, PagesFirstSection, StyledTag } from "@/components";
import { ERoles } from "@/models";

import { CreateRole } from "./CreateRole";
import { RolesAndPermissionsFirstSection } from "./RolesAndPermissionsFirstSection";
import styles from "./styles.module.css";

interface IRole {
    role: ERoles;
    description: string;
    source: "Built-in" | "Custom" | "YAML";
    permissions: string;
    includes?: string;
    members: number;
    lastChange: string;
    changedBy: string;
    viewOnly?: boolean;
}

const roles: IRole[] = [
    {
        role: ERoles.ADMIN,
        description: "Full administration, including users, roles and authentication",
        source: "Built-in",
        permissions: "18 of 18 permissions",
        includes: ERoles.EDITOR,
        members: 7,
        lastChange: "-",
        changedBy: "shipped with Axelix",
    },
    {
        role: ERoles.EDITOR,
        description: "Create and edit dashboards and wallboards, invoke actions",
        source: "Built-in",
        permissions: "12 of 18 permissions",
        includes: ERoles.VIEWER,
        members: 68,
        lastChange: "-",
        changedBy: "shipped with Axelix",
    },
    {
        role: ERoles.VIEWER,
        description: "Read dashboards, wallboards and values",
        source: "Built-in",
        permissions: "5 of 18 permissions",
        members: 337,
        lastChange: "-",
        changedBy: "shipped with Axelix",
    },
    {
        role: ERoles.ADMIN,
        description: "Shift floor staff: launch dashboards and shop-floor wallboards",
        source: "Custom",
        permissions: "7 of 18 permissions",
        includes: ERoles.VIEWER,
        members: 22,
        lastChange: "12 Jun 2026",
        changedBy: "M. Keller",
    },
    {
        role: ERoles.ADMIN,
        description: "Wallboard Operator plus modify on dashboards",
        source: "Custom",
        permissions: "9 of 18 permissions",
        members: 3,
        lastChange: "28 Jul 2026",
        changedBy: "D. Rowe",
    },
    {
        role: ERoles.ADMIN,
        description: "Read values across quality dashboards, export reports",
        source: "Custom",
        permissions: "7 of 18 permissions",
        includes: ERoles.VIEWER,
        members: 14,
        lastChange: "03 Aug 2026",
        changedBy: "D. Rowe",
    },
    {
        role: ERoles.ADMIN,
        description: "Operate and configure the MCP bridge servers",
        source: "YAML",
        permissions: "6 of 18 permissions",
        members: 4,
        lastChange: "01 Aug 2026",
        changedBy: "axelix-roles.yaml",
        viewOnly: true,
    },
    {
        role: ERoles.ADMIN,
        description: "Read-only access everything, including the audit log",
        source: "YAML",
        permissions: "11 of 18 permissions",
        members: 3,
        lastChange: "14 May 2026",
        changedBy: "axelix-roles.yaml",
        viewOnly: true,
    },
];

const RolesAndPermissions = () => {
    const [search, setSearch] = useState<string>("");

    return (
        <>
            <PagesFirstSection
                title="Roles &amp; permissions"
                subtitle="3 built-in · 3 defined in the web UI · 2 managed in axelix-roles.yaml"
                rightContent={<CreateRole />}
            />

            <RolesAndPermissionsFirstSection setSearch={setSearch} addonAfter="Placeholder" />

            <div className="CustomTable">
                <div className={`TableHeader ${styles.TableRow}`}>
                    <div className="TableRowChunk">Role</div>
                    <div className="TableRowChunk">Source</div>
                    <div className="TableRowChunk">Permissions</div>
                    <div className="TableRowChunk">Includes</div>
                    <div className="TableRowChunk">Members</div>
                    <div className="TableRowChunk">Last change</div>
                </div>

                <EmptyHandler isEmpty={roles.length === 0}>
                    {roles.map(
                        ({ role, description, source, permissions, includes, members, lastChange, viewOnly }) => {
                            return (
                                // TODO: Fix the hardcoded role id
                                <Link to="/users/roles-permissions/1" className={`TableRow ${styles.TableRow}`} key={description}>
                                    <div className="TableRowChunk">
                                        <StyledTag>{role}</StyledTag>
                                        {viewOnly && (
                                            <span className={`TextUltraSmall ${styles.ViewOnly}`}>view only</span>
                                        )}
                                        <div className={`TextSmall ${styles.Description}`}>{description}</div>
                                    </div>
                                    <div className="TableRowChunk">
                                        {/* TODO: Redesign */}
                                        <div>{source}</div>
                                    </div>
                                    <div className="TableRowChunk">{permissions}</div>
                                    <div className="TableRowChunk">
                                        <StyledTag>{includes ?? "-"}</StyledTag>
                                    </div>
                                    <div className="TableRowChunk">{members}</div>
                                    <div className="TableRowChunk">
                                        {lastChange}
                                        <div className={`TextUltraSmall ${styles.Description}`}>shipped by Axelix</div>
                                    </div>
                                </Link>
                            );
                        },
                    )}
                </EmptyHandler>
            </div>
        </>
    );
};

export default RolesAndPermissions;
