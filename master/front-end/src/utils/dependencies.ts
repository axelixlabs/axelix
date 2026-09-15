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
import { EDependencyEcosystem, EDependencyNote, EPlatformSupportStatus, ESupportSignal } from "@/models";

/**
 * The support signals in the order they should be presented across the page.
 */
export const SUPPORT_SIGNAL_ORDER: ESupportSignal[] = [ESupportSignal.DISCONTINUED, ESupportSignal.MAINTENANCE];

/**
 * The ecosystems in the order their tabs should be presented, after the leading "All" tab.
 */
export const DEPENDENCY_ECOSYSTEM_ORDER: EDependencyEcosystem[] = [
    EDependencyEcosystem.SPRING,
    EDependencyEcosystem.PERSISTENCE,
    EDependencyEcosystem.SERIALIZATION,
    EDependencyEcosystem.LOGGING,
    EDependencyEcosystem.OBSERVABILITY,
    EDependencyEcosystem.RESILIENCE,
    EDependencyEcosystem.OTHER,
];

/**
 * The i18n key of the short label of a support signal.
 */
export const supportSignalLabelKey: Record<ESupportSignal, string> = {
    [ESupportSignal.DISCONTINUED]: "DependenciesAnalyzer.signals.DISCONTINUED",
    [ESupportSignal.MAINTENANCE]: "DependenciesAnalyzer.signals.MAINTENANCE",
};

/**
 * The CSS-module class token (from the chip / row styles) used to color a support signal.
 */
export const supportSignalClassToken: Record<ESupportSignal, "Discontinued" | "Maintenance"> = {
    [ESupportSignal.DISCONTINUED]: "Discontinued",
    [ESupportSignal.MAINTENANCE]: "Maintenance",
};

/**
 * The accent color of a support signal. Mirrors the {@code --signal-*} CSS variables and is used where those
 * variables are out of scope, e.g. inside a tooltip that is portaled outside the page wrapper.
 */
export const supportSignalColor: Record<ESupportSignal, string> = {
    [ESupportSignal.DISCONTINUED]: "#b42318",
    [ESupportSignal.MAINTENANCE]: "#b54708",
};

/**
 * The i18n key of the label of an ecosystem tab.
 */
export const dependencyEcosystemLabelKey: Record<EDependencyEcosystem, string> = {
    [EDependencyEcosystem.SPRING]: "DependenciesAnalyzer.ecosystems.SPRING",
    [EDependencyEcosystem.PERSISTENCE]: "DependenciesAnalyzer.ecosystems.PERSISTENCE",
    [EDependencyEcosystem.SERIALIZATION]: "DependenciesAnalyzer.ecosystems.SERIALIZATION",
    [EDependencyEcosystem.LOGGING]: "DependenciesAnalyzer.ecosystems.LOGGING",
    [EDependencyEcosystem.OBSERVABILITY]: "DependenciesAnalyzer.ecosystems.OBSERVABILITY",
    [EDependencyEcosystem.RESILIENCE]: "DependenciesAnalyzer.ecosystems.RESILIENCE",
    [EDependencyEcosystem.OTHER]: "DependenciesAnalyzer.ecosystems.OTHER",
};

/**
 * The i18n key of the label a migration note is presented under.
 */
export const dependencyNoteLabelKey: Record<EDependencyNote, string> = {
    [EDependencyNote.SUPERSEDED_BY]: "DependenciesAnalyzer.notes.SUPERSEDED_BY",
    [EDependencyNote.MIGRATION_NOTE]: "DependenciesAnalyzer.notes.MIGRATION_NOTE",
};

/**
 * The i18n key of the badge shown for a platform support status.
 */
export const platformSupportStatusLabelKey: Record<EPlatformSupportStatus, string> = {
    [EPlatformSupportStatus.OSS_SUPPORTED]: "DependenciesAnalyzer.platformStatus.OSS_SUPPORTED",
    [EPlatformSupportStatus.OUT_OF_OSS_MAINTENANCE]: "DependenciesAnalyzer.platformStatus.OUT_OF_OSS_MAINTENANCE",
};

/**
 * The i18n key of the "what this means for you" prose shown next to a platform support status.
 */
export const platformSupportConsequenceKey: Record<EPlatformSupportStatus, string> = {
    [EPlatformSupportStatus.OSS_SUPPORTED]: "DependenciesAnalyzer.consequences.OSS_SUPPORTED",
    [EPlatformSupportStatus.OUT_OF_OSS_MAINTENANCE]: "DependenciesAnalyzer.consequences.OUT_OF_OSS_MAINTENANCE",
};

/**
 * The date format the maintenance window renders full dates in (e.g. {@code 23 Nov 2023}).
 */
export const DEPENDENCY_DATE_FORMAT = "DD MMM YYYY";

/**
 * The date format the maintenance window renders month-precision dates in (e.g. {@code Jun 2027}).
 */
export const DEPENDENCY_MONTH_FORMAT = "MMM YYYY";
