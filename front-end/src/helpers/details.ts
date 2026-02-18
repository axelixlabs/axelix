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
import type { FC, SVGProps } from "react";

import { ECopyableField, type IDetailsRuntime } from "models";

import { FreeBSDIcon, JavaIcon, KotlinIcon, LinuxIcon, WindowsIcon } from "assets";

/**
 * Resolve the Icon to be used on the details card for the given Operating System.
 *
 * @param osName operating system name
 */
export function resolveOsIcon(osName: string): FC<SVGProps<SVGSVGElement>> {
    switch (osName.trim().toLowerCase()) {
        case "windows":
            return WindowsIcon;
        case "freebsd":
            return FreeBSDIcon;
        default:
            return LinuxIcon;
    }
}

export function isCopyableField(field: string): boolean {
    return (Object.values(ECopyableField) as string[]).includes(field);
}

/**
 * Resolve the Icon to be used on the details card for the given runtime.
 *
 * @param runtime the runtime information
 */
export function resolveLangIcon(runtime: IDetailsRuntime): FC<SVGProps<SVGSVGElement>> {
    return runtime.kotlinVersion ? KotlinIcon : JavaIcon;
}
