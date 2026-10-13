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
    { text: "Make sure your ", highlighted: false },
    { text: "Java", highlighted: true },
    { text: " microservices are ready for the ", highlighted: false },
    { text: "AI", highlighted: true },
    { text: " era.", highlighted: false },
] as const;

export const HERO_INTRO_SEGMENTS = [
    { text: "The ", highlighted: false },
    { text: "AI-Native", highlighted: true },
    { text: ", Open Core solution for the AI era: its open-source core ", highlighted: false },
    { text: "pinpoints", highlighted: true },
    { text: " what holds your Java back, while Axelix Enterprise ", highlighted: false },
    { text: "enforces", highlighted: true },
    { text: " optimization policies across the whole ecosystem. ", highlighted: false },
    { text: "At scale", highlighted: true },
    { text: ".", highlighted: false },
] as const;

export const HEADLINE = HERO_HEADLINE_SEGMENTS.map(({ text }) => text).join("");
export const INTRO = HERO_INTRO_SEGMENTS.map(({ text }) => text).join("");

export const DESCRIPTION =
    "Axelix streamlines the development of the Java Spring Boot ecosystems by ensuring the quality and observability across the entire Java fleet";
