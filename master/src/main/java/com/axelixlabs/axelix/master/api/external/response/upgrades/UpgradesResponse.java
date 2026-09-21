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
package com.axelixlabs.axelix.master.api.external.response.upgrades;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * How far Axelix Master can be safely upgraded without any service in the fleet falling out of the starter
 * compatibility window.
 *
 * @param masterVersion         the version of Axelix Master currently running.
 * @param servicesTotal         the number of distinct services seen at least once in the last 30 days.
 * @param oldestStarterVersion  the {@code major.minor} of the oldest starter version currently in use, or
 *                              {@code null} when no service has been seen.
 * @param compatibilityWindow   the number of consecutive minor releases (inclusive) a starter stays compatible
 *                              with Master for.
 * @param safeUpgradeCeiling    the highest {@code major.minor} Master version every currently-seen service would
 *                              stay compatible with, or {@code null} when no service has been seen.
 * @param starterVersions       the fleet's starter versions, grouped by {@code major.minor}, newest first.
 * @param ceilingBlockers       the services running the oldest starter version, i.e. the ones holding the safe
 *                              upgrade ceiling back.
 *
 * @author Nikita Kirillov
 */
public record UpgradesResponse(
        String masterVersion,
        int servicesTotal,
        @Nullable String oldestStarterVersion,
        int compatibilityWindow,
        @Nullable String safeUpgradeCeiling,
        List<StarterVersionUsage> starterVersions,
        List<CeilingBlocker> ceilingBlockers) {}
