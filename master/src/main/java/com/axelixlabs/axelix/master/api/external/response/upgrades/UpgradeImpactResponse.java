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

/**
 * The impact of upgrading Axelix Master to a given version on the fleet.
 *
 * @param targetVersion    the version Axelix Master is going to be upgraded to.
 * @param lostApplications the number of applications whose starter version falls out of the compatibility
 *                         window of the {@code targetVersion}, i.e. that Axelix Master would no longer serve.
 *
 * @author Nikita Kirillov
 */
public record UpgradeImpactResponse(String targetVersion, int lostApplications) {}
