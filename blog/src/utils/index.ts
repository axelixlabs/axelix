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
import { chipColorStyle } from "@/helpers";
import type { INavLink } from "@/models"

export const VISIBLE_TAG_COUNT = 6;
export const DEFAULT_CHIP_STYLE = chipColorStyle("var(--ink-4)");

export const NAV_LINKS: INavLink[] = [
    { href: "https://axelix.io/#reference-app", label: "Why Axelix?" },
    { href: "https://axelix.io/#capabilities", label: "Debugging" },
    { href: "https://axelix.io/#install", label: "Install" },
    { href: "https://axelix.io/#enterprise", label: "Enterprise" },
    { href: "https://axelix.io/#faq", label: "FAQ" },
];