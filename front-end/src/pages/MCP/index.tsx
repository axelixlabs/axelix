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
    const [MCPToolsData, setMCPToolsData] = useState(StatefulRequest.loading<IMCPToolsResponseBody>());

    useEffect(() => {
        fetchData(setMCPToolsData, () => getMCPTools());
    }, []);

    if (MCPToolsData.loading) {
        return <Loader />;
    }

    if (MCPToolsData.error) {
        return <EmptyHandler isEmpty />;
    }

    const MCPTools = MCPToolsData.response!.tools;
    const effectiveMCPTools = search ? filterMCPTools(MCPTools, search) : MCPTools;

    const addonAfter = `${effectiveMCPTools.length} / ${MCPTools.length}`;

    return (
        <>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} />

            <EmptyHandler isEmpty={effectiveMCPTools.length === 0}>
                <div className={styles.CardsWrapper}>
                    {effectiveMCPTools.map((MCPTool) => (
                        <MCPCard MCPTool={MCPTool} key={MCPTool.title} />
                    ))}
                </div>
            </EmptyHandler>
        </>
    );
};

export default MCP;
