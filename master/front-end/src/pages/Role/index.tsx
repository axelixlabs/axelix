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
import styles from "./styles.module.css";
import { PagesFirstSection, StyledTag } from "@/components";
import { Button } from "antd";

const roleItems = [
    {
        name: "ADMIN",
        usersCount: 2,
        title: "Full administration: users, roles, authentication and all content.",
        description: "Read, read values and modify on every resource",
    },
    {
        name: "EDITOR",
        usersCount: 9,
        title: "Create and edit dashboards and wallboards, invoke MCP actions.",
        description: "Read + read values on all resources, modify on dashboards and wallboards",
    },
    {
        name: "VIEWER",
        usersCount: 31,
        title: "Read dashboards, wallboards and their values.",
        description: "Read and read values on dashboards and wallboards",
    },
];

const Role = () => {
    return (
        <>
            <PagesFirstSection title="Roles &amp; permissions" subtitle="Community edition · three built-in roles, assignable on the Users page" />
            <div className={styles.RolesList}>
                {roleItems.map(({ title, description, name, usersCount }) => (
                    <div key={name} className={styles.RoleRow}>
                        <div>
                            <StyledTag>
                                {name}
                            </StyledTag>
                            <div className={styles.RoleUsersCount}>{usersCount} users</div>
                        </div>
                        <div>
                            <div className={styles.RoleTitle}>{title}</div>
                            <div className={styles.RoleDescription}>{description}</div>
                        </div>
                    </div>
                ))}
            </div>

            <div className={styles.EnterpriseCard}>
                <div className={styles.EnterpriseContent}>
                    <div className={styles.EnterpriseTitle}>Custom roles are an Enterprise feature</div>
                    <div className={`TextSmall ${styles.EnterpriseText}`}>
                        Define your own roles from individual permissions, compose roles from other roles, and manage
                        them as code in a YAML file.
                    </div>
                </div>

                <Button type="primary">
                    Compare editions
                </Button>
            </div>
        </>
    );
};

export default Role;
