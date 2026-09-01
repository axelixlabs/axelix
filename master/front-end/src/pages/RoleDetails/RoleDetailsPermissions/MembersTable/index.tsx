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
import { Avatar } from "antd";
import { Link } from "react-router";

import { AdministrationTable } from "@/components";
import { getInitials } from "@/helpers";

import styles from "./styles.module.css";

const members = [
    {
        name: "Anja Novak",
        source: "direct",
    },
    {
        name: "Piotr Zieliński",
        source: "direct",
    },
    {
        name: "Sofia Ríos",
        source: "direct",
    },
    {
        name: "Mei Tanaka",
        source: "Shift Supervisor",
    },
];

export const MembersTable = () => {
    return (
        <>
            <AdministrationTable
                title="Members"
                headerSecondColumn={
                    <Link to="#" className="AccentedLink">
                        View all 22
                    </Link>
                }
            >
                {members.map(({ name, source }) => {
                    return (
                        <div className={`TableRow ${styles.MemberRow}`}>
                            <div className={`TableRowChunk ${styles.MemberInfo}`}>
                                <Avatar>{getInitials(name)}</Avatar>
                                <div>{name}</div>
                            </div>
                            <div className={`TableRowChunk ${styles.MemberSource}`}>{source}</div>
                        </div>
                    );
                })}
            </AdministrationTable>
        </>
    );
};
