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
package contract.invariants.stateful

import contract.ContractSchema
import contract.ContractSchema.Companion.INTRODUCED_IN

/**
 * A schema or a property the latest release does not know of is being born right now, so its
 * 'x-axelix-introduced-in' must name the version currently being built. A backdated marker
 * would immediately mislead the window arithmetic: the part would look like an old promise
 * that released Masters and starters already rely on, while in reality nothing released has
 * ever seen it.
 *
 * An absent or malformed marker is not reported here: the stateless invariants already do.
 *
 * @author Mikhail Polivakha
 */
object NewPartsMustBeMarkedAsIntroducedInTheCurrentRelease : StatefulContractInvariant {

    override fun check(baseline: ContractSchema?, current: ContractSchema?): List<String> {
        if (current == null) {
            return emptyList()
        }

        val unreleasedMarkers = if (baseline == null) {
            current.markedParts
        } else {
            val releasedProperties = baseline.properties.map { property -> property.name }.toSet()
            current.properties
                .filter { property -> property.name !in releasedProperties }
                .map { property -> ContractSchema.MarkedPart(property.location, property.node) }
        }

        return unreleasedMarkers.mapNotNull { part ->
            val introduced = current.markerVersion(part.node, INTRODUCED_IN) ?: return@mapNotNull null

            if (introduced != current.axelixVersion) {
                ("${part.location} is born in this release but has '$INTRODUCED_IN: $introduced' "
                    + "instead of the version currently being built (${current.axelixVersion})")
            } else {
                null
            }
        }
    }
}
