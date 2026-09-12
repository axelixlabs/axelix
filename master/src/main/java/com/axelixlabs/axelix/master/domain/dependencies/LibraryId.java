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
package com.axelixlabs.axelix.master.domain.dependencies;

/**
 * The stable identifier of a curated project, e.g. {@code spring-cloud-sleuth}. It is chosen by whoever writes the
 * catalog entry and must never change afterwards, because it is what the front-end keys presentation on and what any
 * future cross-reference between entries would point at.
 * <p>
 * It identifies the <em>project</em>, not an artifact: one id owns every {@link ArtifactCoordinates} the project
 * publishes.
 *
 * @param value the identifier, in lower kebab-case
 *
 * @author Mikhail Polivakha
 */
public record LibraryId(String value) {

    public static LibraryId of(String value) {
        return new LibraryId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
