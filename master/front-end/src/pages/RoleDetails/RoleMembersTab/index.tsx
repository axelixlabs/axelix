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
import { Avatar, Button } from "antd";
import { useState } from "react";
import { Link } from "react-router";

import { PageSearch } from "@/components";
import { getInitials } from "@/helpers";

import styles from "./styles.module.css";

export const RoleMembersTab = () => {
    const [, setSearch] = useState<string>("");

    return (
        <>
            <div className={styles.MembersToolbar}>
                <PageSearch setSearch={setSearch} addonAfter="Placeholder" />
                <Button type="primary">Add members</Button>
            </div>

            <div className="CustomTable">
                <div className={`TableHeader ${styles.TableRow}`}>
                    <div className="TableRowChunk">Member</div>
                    <div className="TableRowChunk">Department</div>
                    <div className="TableRowChunk">Held</div>
                    <div className="TableRowChunk">Last activity</div>
                    <div />
                </div>

                <div className={`TableRow ${styles.TableRow}`}>
                    <div className="TableRowChunk">
                        <div className={styles.MemberInfoWrap}>
                            <Avatar>{getInitials("Anja Novak")}</Avatar>
                            <div>
                                <div className={styles.MemberName}>Anja Novak</div>
                                <div className={`TextSmall ${styles.MemberEmail}`}>anja.novak@example.com</div>
                            </div>
                        </div>
                    </div>
                    <div className="TableRowChunk">Operations</div>
                    <div className="TableRowChunk">Direct</div>
                    <div className="TableRowChunk">Yesterday</div>
                    <div className="TableRowChunk">
                        <Link to="#" className="AccentedLink">
                            Remove
                        </Link>
                    </div>
                </div>
            </div>
        </>
    );
};
