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
import type { EMCPToolStatus } from "models";

export interface IMCPAnnotation {
    /**
     * true if mcp tool does not modify any data, false otherwise
     */
    readOnlyHint: boolean;

    /**
     * true if mcp tool performs destructive actions, false otherwise
     */
    destructiveHint: boolean;

    /**
     * true if the mcp tool is idempotent, false otherwise
     */
    idempotentHint: boolean;

    /**
     * true if the tool interacts with open world, false otherwise.
     */
    openWorldHint: boolean;
}

export interface IMCPTool {
    /**
     * MCP tool title
     */
    title: string;

    /**
     * MCP tool description
     */
    description: string;

    /**
     * MCP tool annotations
     */
    annotations: IMCPAnnotation;

    /**
     * MCP tool status
     */
    status: EMCPToolStatus;
}

export interface IMCPToolsResponseBody {
    /**
     * List of MCP tools
     */
    tools: IMCPTool[];
}
