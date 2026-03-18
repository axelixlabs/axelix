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
     * Indicates that the tool does not modify any data
     */
    readOnlyHint: boolean;

    /**
     * Indicates that the tool performs destructive actions
     */
    destructiveHint: boolean;

    /**
     * Indicates that repeated calls produce the same result
     */
    idempotentHint: boolean;

    /**
     * Indicates that the tool may interact with external systems
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
