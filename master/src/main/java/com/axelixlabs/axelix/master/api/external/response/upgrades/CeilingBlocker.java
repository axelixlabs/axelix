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

import java.time.LocalDate;

/**
 * A service running the oldest starter version observed in the fleet, i.e. one that holds the safe upgrade
 * ceiling back.
 *
 * @param artifactId     the artifact id of the service.
 * @param groupId        the group id of the service.
 * @param starterVersion the exact starter version the service was last seen running, e.g. {@code 1.2.1}.
 * @param lastSeen       the date the service was last seen on.
 *
 * @author Nikita Kirillov
 */
public record CeilingBlocker(String artifactId, String groupId, String starterVersion, LocalDate lastSeen) {}
