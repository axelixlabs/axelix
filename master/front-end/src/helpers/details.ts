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

/**
 * Resolve the short initials shown in the tile next to the instance name, e.g. "sp" for "spring-petclinic".
 *
 * @param serviceName the name of the service
 */
export function resolveInitials(serviceName: string): string {
    const words = serviceName.split(/[^a-zA-Z0-9]+/).filter(Boolean);

    if (words.length > 1) {
        return (words[0][0] + words[1][0]).toLowerCase();
    }

    return serviceName.slice(0, 2).toLowerCase();
}
