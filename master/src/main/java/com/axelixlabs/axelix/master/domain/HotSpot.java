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
package com.axelixlabs.axelix.master.domain;

import java.util.List;

/**
 * HotSpot-specific insight groups.
 *
 * @param projectLeyden    the Project Leyden insight features
 * @param gc               the garbage collection insight features
 * @param projectLilliput the Project Lilliput insight features
 *
 * @author Mikhail Polivakha
 */
public record HotSpot(
        List<InsightFeature> projectLeyden, List<InsightFeature> gc, List<InsightFeature> projectLilliput) {

    public static HotSpot empty() {
        return new HotSpot(List.of(), List.of(), List.of());
    }
}
