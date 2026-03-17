import { EmptyHandler, PageSearch } from "components"
import { useState } from "react";
import styles from "./styles.module.css"
import { MCPCard } from "./MCPCard";
import { filterMCPTools } from "helpers/mcp";

const MCP = () => {
    const mockMCPTools = [
        {
            title: "Title 1",
            description: "Description",
            annotation: "Annotation",
            isEnable: true,
            viewAccessLog: "Access Log"
        },
        {
            title: "Title 2",
            description: "Description",
            annotation: "Annotation",
            isEnable: true,
            viewAccessLog: "Access Log"
        },
        {
            title: "Title 3",
            description: "Description",
            annotation: "Annotation",
            isEnable: true,
            viewAccessLog: "Log"
        }
    ]

    const [search, setSearch] = useState<string>("")
    const effectiveMCPTools = search ? filterMCPTools(mockMCPTools, search) : mockMCPTools;

    const addonAfter = `${effectiveMCPTools.length} / ${mockMCPTools.length}`;

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
    )
}

export default MCP
