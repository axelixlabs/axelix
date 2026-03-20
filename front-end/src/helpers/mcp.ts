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
import { EMCPCardChunks, type IMCPTool } from "models";

export const filterMCPTools = (MCPTools: IMCPTool[], search: string): IMCPTool[] => {
    const formattedSearch = search.toLowerCase().trim();

    return MCPTools.filter(({ title }) => {
        const lowerTitle = title.toLowerCase();
        if (lowerTitle.includes(formattedSearch)) {
            return true;
        }
    });
};

/**
 * Synchronizes MCP card heights by internal segments (chunks).
 * Finds max height of headers/descriptions and sets them as CSS variables.
 */
export const synchronizeMCPCardChunksHeights = (cardsWrapper: HTMLElement | null): void => {
    if (!cardsWrapper) {
        return;
    }

    cardsWrapper.style.removeProperty("--height-header");
    cardsWrapper.style.removeProperty("--height-description");

    requestAnimationFrame(() => {
        const allHeaders = Array.from(
            cardsWrapper.querySelectorAll(`[data-card-chunk=${EMCPCardChunks.HEADER}]`),
        ) as HTMLElement[];

        const allDescriptions = Array.from(
            cardsWrapper.querySelectorAll(`[data-card-chunk=${EMCPCardChunks.DESCRIPTION}]`),
        ) as HTMLElement[];

        const allHeadersHeights = allHeaders.map(({ offsetHeight }) => offsetHeight);
        const allDescriptionsHeights = allDescriptions.map(({ offsetHeight }) => offsetHeight);

        const maxHeaderHeight = Math.max(...allHeadersHeights);
        const maxDescriptionHeight = Math.max(...allDescriptionsHeights);

        cardsWrapper.style.setProperty("--height-header", `${maxHeaderHeight}px`);
        cardsWrapper.style.setProperty("--height-description", `${maxDescriptionHeight}px`);
    });
};
