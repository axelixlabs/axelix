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

import contract.ContractDocument
import contract.ContractDocument.Companion.INTRODUCED_IN

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

    override fun check(baseline: ContractDocument?, current: ContractDocument): List<String> {
        val unreleasedMarkers = if (baseline == null) {
            // nothing was released, so every schema and every property is new
            current.schemas.map { schema -> ContractDocument.MarkedPart(schema.location, schema.node) } +
                current.properties.map { property -> ContractDocument.MarkedPart(property.location, property.node) }
        } else {
            val releasedSchemas = baseline.schemas.map { schema -> schema.name }.toSet()
            val releasedProperties = baseline.properties.map { property -> property.schemaName to property.name }.toSet()
            current.schemas
                .filter { schema -> schema.name !in releasedSchemas }
                .map { schema -> ContractDocument.MarkedPart(schema.location, schema.node) } +
                current.properties
                    .filter { property -> (property.schemaName to property.name) !in releasedProperties }
                    .map { property -> ContractDocument.MarkedPart(property.location, property.node) }
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
