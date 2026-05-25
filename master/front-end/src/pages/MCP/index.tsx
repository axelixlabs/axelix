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
import { useEffect, useState } from "react";
import { getMCPTools } from "services/MCP";

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData } from "helpers";
import { filterMCPTools } from "helpers/mcp";
import { type IMCPToolsResponseBody, StatefulRequest } from "models";

import { MCPCard } from "./MCPCard";
import styles from "./styles.module.css";

const MCP = () => {
    const [search, setSearch] = useState<string>("");
    const [mcpToolsData, setMcpToolsData] = useState(StatefulRequest.loading<IMCPToolsResponseBody>());

    useEffect(() => {
        fetchData(setMcpToolsData, () => getMCPTools());
    }, []);

    if (mcpToolsData.loading) {
        return <Loader />;
    }

    if (mcpToolsData.error) {
        return <EmptyHandler isEmpty />;
    }

    const mcpTools = mcpToolsData.response!.tools;
    const effectiveMCPTools = search ? filterMCPTools(mcpTools, search) : mcpTools;

    const addonAfter = `${effectiveMCPTools.length} / ${mcpTools.length}`;

    return (
        <>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} />

            <EmptyHandler isEmpty={effectiveMCPTools.length === 0}>
                <div className={styles.CardsWrapper}>
                    {effectiveMCPTools.map((mcpTool) => (
                        <MCPCard mcpTool={mcpTool} key={mcpTool.title} />
                    ))}
                </div>
            </EmptyHandler>
        </>
    );
};

export default MCP;
