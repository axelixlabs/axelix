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
import { Badge, Tooltip } from "antd";
import { Fragment, useEffect, useRef, useState } from "react";

import { EMCPToolStatus, type IMCPTool } from "models";

import { getExtension } from "../../../../extensions.ts";
import { MCPCardDescription } from "../MCPCardDescription";
import { MCPCardFooter } from "../MCPCardFooter";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single MCP tool data
     */
    mcpTool: IMCPTool;
}

export const MCPCard = ({ mcpTool }: IProps) => {
    const MCPAccessLogComponent = getExtension("MCPAccessLog") ?? Fragment;

    const textRef = useRef<HTMLDivElement>(null);

    const [isEllipsis, setIsEllipsis] = useState<boolean>(false);

    const { title, description, annotations, status } = mcpTool;

    useEffect(() => {
        const element = textRef.current;

        if (!element) {
            return;
        }

        setIsEllipsis(element.scrollHeight > element.clientHeight + 1);
    }, []);

    const isEnabled = status === EMCPToolStatus.UP;

    return (
        <>
            <MCPAccessLogComponent>
                <div className={styles.Card}>
                    <div className={`${styles.Header} ${isEllipsis ? styles.TwoLinesHeader : ""}`}>
                        <Tooltip title={isEllipsis ? title : undefined}>
                            <div ref={textRef} className={styles.Title}>
                                {title}
                            </div>
                        </Tooltip>
                        <Badge
                            color={isEnabled ? "#00ab55" : "#ff000a"}
                            styles={{
                                indicator: {
                                    width: "8px",
                                    height: "8px",
                                },
                            }}
                        />
                    </div>

                    <MCPCardDescription description={description} />

                    <MCPCardFooter annotations={annotations} />
                </div>
            </MCPAccessLogComponent>
        </>
    );
};
