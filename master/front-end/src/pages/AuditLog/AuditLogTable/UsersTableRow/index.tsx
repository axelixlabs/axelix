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
import { Badge } from "antd";

import sharedStyles from "../shared.module.css";

import { StyledTag, AdministrationAvatarWithData } from "@/components";

export const AuditLogTableRow = () => {
    return (
        <>
            <div className={`TableRow ${sharedStyles.TableRow}`}>
                <div className="TableRowChunk">
                    <AdministrationAvatarWithData primaryText="Dana Rowe" />
                </div>
                <div className="TableRowChunk">
                    org.springframework.samples:spring-petclinic
                </div>
                <div className="TableRowChunk">
                    <StyledTag>beans:read</StyledTag>
                </div>
                <div className="TableRowChunk">
                    <Badge status="success" text="Success" />
                </div>
                <div className="TableRowChunk">
                    <StyledTag>WEB</StyledTag>
                </div>
                <div className="TableRowChunk">12 min ago</div>
            </div>
        </>
    );
};
