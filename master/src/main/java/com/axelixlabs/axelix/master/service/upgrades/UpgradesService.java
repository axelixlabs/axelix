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
package com.axelixlabs.axelix.master.service.upgrades;

import com.axelixlabs.axelix.master.api.external.response.upgrades.UpgradeImpactResponse;
import com.axelixlabs.axelix.master.api.external.response.upgrades.UpgradesResponse;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;

/**
 * Computes how far Axelix Master can be safely upgraded, given the starter versions currently in use across the
 * fleet.
 *
 * @author Nikita Kirillov
 */
public interface UpgradesService {

    /**
     * @return how far Axelix Master can be safely upgraded, given the starter versions currently in use across
     *         the fleet.
     */
    UpgradesResponse getUpgrades();

    /**
     * @param targetVersion the {@code major.minor} version Axelix Master is going to be upgraded to.
     * @return how many applications would lose compatibility with Axelix Master after the upgrade.
     * @throws BadRequestException if the {@code targetVersion} is not a valid {@code major.minor} version.
     */
    UpgradeImpactResponse getUpgradeImpact(String targetVersion);
}
