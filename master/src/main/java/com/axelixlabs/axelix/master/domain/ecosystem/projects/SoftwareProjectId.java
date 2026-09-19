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
package com.axelixlabs.axelix.master.domain.ecosystem.projects;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;

/**
 * The stable identifier of a curated project, e.g. {@code spring-cloud-sleuth}.
 * <p>
 * It identifies the <em>project</em>, not an artifact: one id owns every {@link Library} the project
 * publishes.
 *
 * @param value the identifier, in lower kebab-case
 *
 * @author Mikhail Polivakha
 */
public record SoftwareProjectId(String value) {

    public static SoftwareProjectId of(String value) {
        return new SoftwareProjectId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
