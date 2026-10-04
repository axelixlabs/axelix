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
package com.axelixlabs.axelix.master.service.ecosystem.platform;

import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;

/**
 * The read side of the curated platform support policies: given a platform name, what does Axelix know about its
 * release lines and their maintenance windows.
 *
 * @author Mikhail Polivakha
 */
public interface PlatformCatalog {

    /**
     * @param platformName the name of the platform.
     *
     * @throws IllegalStateException in case the platform is not found. The platforms exist as the pre-condition,
     *         and they represent a set with the well-known values. So any requested platform must be found.
     *
     * @return the curated policy of the platform
     */
    Platform find(PlatformName platformName) throws IllegalStateException;
}
