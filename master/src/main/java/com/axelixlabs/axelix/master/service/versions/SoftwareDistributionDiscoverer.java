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
package com.axelixlabs.axelix.master.service.versions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.domain.BuildInfo;
import com.axelixlabs.axelix.master.model.software.SoftwareComponent;
import com.axelixlabs.axelix.master.model.software.SoftwareDistribution;

/**
 * The SPI interface that is capable to extract the information about specific {@link SoftwareDistribution}
 * inside the given {@link BuildInfo} about the given {@link SoftwareComponent}.
 *
 * @author Mikhail Polivakha
 */
public interface SoftwareDistributionDiscoverer<T extends SoftwareComponent> {

    @Nullable
    SoftwareDistribution discover(@NonNull BuildInfo buildInfo);
}
