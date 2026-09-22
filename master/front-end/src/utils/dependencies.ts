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
import { EDependencyEcosystem, EFrameworkSupportStatus, type EProblemSupportStatus, ESupportStatus } from "@/models";

export const SUPPORT_STATUS_ORDER: EProblemSupportStatus[] = [
    ESupportStatus.SUNSET,
    ESupportStatus.MAINTENANCE,
    ESupportStatus.DORMANT,
];

export const DEPENDENCY_ECOSYSTEM_ORDER: EDependencyEcosystem[] = [
    EDependencyEcosystem.SPRING,
    EDependencyEcosystem.PERSISTENCE,
    EDependencyEcosystem.SERIALIZATION,
    EDependencyEcosystem.LOGGING,
    EDependencyEcosystem.OBSERVABILITY,
    EDependencyEcosystem.RESILIENCE,
    EDependencyEcosystem.OTHER,
];

export const supportSignalLabelKey: Record<EProblemSupportStatus, string> = {
    [ESupportStatus.SUNSET]: "DependenciesAnalyzer.statuses.SUNSET",
    [ESupportStatus.MAINTENANCE]: "DependenciesAnalyzer.statuses.MAINTENANCE",
    [ESupportStatus.DORMANT]: "DependenciesAnalyzer.statuses.DORMANT",
};

export const supportSignalColor: Record<EProblemSupportStatus, string> = {
    [ESupportStatus.SUNSET]: "#b42318",
    [ESupportStatus.MAINTENANCE]: "#b54708",
    [ESupportStatus.DORMANT]: "#faad14",
};

export const dependencyEcosystemLabelKey: Record<EDependencyEcosystem, string> = {
    [EDependencyEcosystem.SPRING]: "DependenciesAnalyzer.ecosystems.SPRING",
    [EDependencyEcosystem.PERSISTENCE]: "DependenciesAnalyzer.ecosystems.PERSISTENCE",
    [EDependencyEcosystem.SERIALIZATION]: "DependenciesAnalyzer.ecosystems.SERIALIZATION",
    [EDependencyEcosystem.LOGGING]: "DependenciesAnalyzer.ecosystems.LOGGING",
    [EDependencyEcosystem.OBSERVABILITY]: "DependenciesAnalyzer.ecosystems.OBSERVABILITY",
    [EDependencyEcosystem.RESILIENCE]: "DependenciesAnalyzer.ecosystems.RESILIENCE",
    [EDependencyEcosystem.OTHER]: "DependenciesAnalyzer.ecosystems.OTHER",
};

export const frameworkSupportStatusLabelKey: Record<EFrameworkSupportStatus, string> = {
    [EFrameworkSupportStatus.OSS_SUPPORTED]: "DependenciesAnalyzer.frameworkStatus.OSS_SUPPORTED",
    [EFrameworkSupportStatus.OUT_OF_OSS_MAINTENANCE]: "DependenciesAnalyzer.frameworkStatus.OUT_OF_OSS_MAINTENANCE",
};

export const frameworkSupportConsequenceKey: Record<EFrameworkSupportStatus, string> = {
    [EFrameworkSupportStatus.OSS_SUPPORTED]: "DependenciesAnalyzer.consequences.OSS_SUPPORTED",
    [EFrameworkSupportStatus.OUT_OF_OSS_MAINTENANCE]: "DependenciesAnalyzer.consequences.OUT_OF_OSS_MAINTENANCE",
};

export const DEPENDENCY_DATE_FORMAT = "DD MMM YYYY";

export const DEPENDENCY_MONTH_FORMAT = "MMM YYYY";
