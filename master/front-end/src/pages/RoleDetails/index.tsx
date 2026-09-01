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
import { Breadcrumb, Button, Tabs, type TabsProps } from "antd";

import { PagesFirstSection } from "@/components";

import { RoleDetailsPermissions } from "./RoleDetailsPermissions";
import { RoleMembersTab } from "./RoleMembersTab";
import styles from "./styles.module.css";
import { DeleteRole } from "./DeleteRole";

const RoleDetails = () => {
    const tabItems: TabsProps["items"] = [
        {
            key: "permissions",
            label: "Permissions",
            children: <RoleDetailsPermissions />,
        },
        {
            key: "members",
            label: "Members 22",
            children: <RoleMembersTab />,
        },
    ];

    return (
        <>
            <Breadcrumb
                items={[
                    {
                        title: "Roles & permissions",
                    },
                    {
                        title: "Wallboard Operator",
                    },
                ]}
            />

            <PagesFirstSection
                title="Wallboard Operator"
                subtitle="Shift floor staff: read dashboard values and modify wallboards."
                rightContent={
                    <div className={styles.ActionButtonsWrapper}>
                        <Button>Clone</Button>
                        <Button>Edit permissions</Button>
                        <DeleteRole />
                    </div>
                }
            />

            <Tabs defaultActiveKey="permissions" items={tabItems} />
        </>
    );
};

export default RoleDetails;
