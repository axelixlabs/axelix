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
 * What a curated project registry states about the project behind a resolved dependency. A dependency without a
 * signal is simply a project nothing is flagged about, it is not a statement that the project is healthy.
 */
export enum ESupportStatus {
    ACTIVE = "ACTIVE",
    MAINTENANCE = "MAINTENANCE",
    SUNSET = "SUNSET",
    DORMANT = "DORMANT",
}

/**
 * The area of the runtime a resolved dependency belongs to. Used to group the dependency feed into tabs.
 */
export enum EDependencyEcosystem {
    SPRING = "SPRING",
    PERSISTENCE = "PERSISTENCE",
    SERIALIZATION = "SERIALIZATION",
    LOGGING = "LOGGING",
    OBSERVABILITY = "OBSERVABILITY",
    RESILIENCE = "RESILIENCE",
    OTHER = "OTHER",
}

/**
 * Where the platform version an instance runs sits on the upstream OSS maintenance window.
 */
export enum EFrameworkSupportStatus {
    OSS_SUPPORTED = "OSS_SUPPORTED",
    OUT_OF_OSS_MAINTENANCE = "OUT_OF_OSS_MAINTENANCE",
}
