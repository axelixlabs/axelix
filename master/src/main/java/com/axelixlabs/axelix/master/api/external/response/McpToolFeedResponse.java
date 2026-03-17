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
package com.axelixlabs.axelix.master.api.external.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The tools feed is registered.
 *
 * @param tools the list of all registered MCP tools.
 *
 * @author Sergey Cherkasov
 */
public record McpToolFeedResponse(@JsonProperty("tools") List<Tool> tools) {

    /**
     * Represents a tool that the server provides.
     *
     * @param title        The name of the tool. If not provided, the method name will be used.
     * @param description  The description of the tool. If not provided, the method name will be used.
     * @param annotations  Optional additional tool information.
     * @param status       The state of the given MCP tool.
     */
    public record Tool(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("annotations") ToolAnnotations annotations,
            @JsonProperty("status") ToolStatus status) {}

    /**
     * Additional properties describing a Tool to clients.
     *
     * @param readOnlyHint      If {@code true), the tool does not modify its environment. (default {@code false)
     * @param destructiveHint   If {@code true), the tool may perform destructive updates to its environment.
     *                          If {@code  false}, the tool performs only additive updates. (default {@code true)
     * @param idempotentHint    If {@code true), calling the tool repeatedly with the same arguments will have no
     *                          additional effect on the its environment. (default {@code false)
     * @param openWorldHint     If {@code true), this tool may interact with an “open world” of external entities.
     *                          If {@code  false}, the tool’s domain of interaction is closed. For example, the world
     *                          of a web search tool is open, whereas that of a memory tool is not. (default {@code true)
     */
    public record ToolAnnotations(
            @JsonProperty("readOnlyHint") Boolean readOnlyHint,
            @JsonProperty("destructiveHint") Boolean destructiveHint,
            @JsonProperty("idempotentHint") Boolean idempotentHint,
            @JsonProperty("openWorldHint") Boolean openWorldHint) {}

    /**
     * The state of the given MCP tool.
     */
    public enum ToolStatus {
        UP,
        DISABLE
    }
}
