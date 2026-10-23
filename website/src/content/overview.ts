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
export const HERO_HEADLINE_SEGMENTS = [
    { text: "See what your ", highlighted: false },
    { text: "Spring Boot", highlighted: true },
    { text: " apps are really doing in production, and ", highlighted: false },
    { text: "what's wrong", highlighted: true },
    { text: " with them", highlighted: false },
] as const;

// Feeds llms.txt (INTRO below); the hero itself no longer renders this text.
export const HERO_INTRO_SEGMENTS: readonly { text: string; href?: string }[] = [
    {
        text:
            "Axelix shows the live state of every running service: which property value won and where it came from, " +
            "why each bean exists or doesn't, transactions, loggers and dependencies. It flags the misconfigurations " +
            "and anti-patterns that pass every test and still hurt you in prod, like Open Session In View, N+1 queries " +
            "and eager fetching. With ",
    },
    { text: "Axelix Enterprise", href: "/enterprise" },
    {
        text: ", you can stop them from reaching production at all. For you in the web console, and for your AI agent through MCP.",
    },
];

export const HEADLINE = HERO_HEADLINE_SEGMENTS.map(({ text }) => text).join("");
export const INTRO = HERO_INTRO_SEGMENTS.map(({ text }) => text).join("");

export const DESCRIPTION =
    "Axelix streamlines the development of the Java Spring Boot ecosystems by ensuring the quality and observability across the entire Java fleet";
