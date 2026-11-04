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
package contract.invariants.stateless

import contract.ContractSchema
import contract.ContractSchema.Companion.DEPRECATED_IN
import contract.ContractSchema.Companion.INTRODUCED_IN
import contract.Version

/**
 * Every lifecycle marker must be of the x.y.z form and must not be ahead of the version
 * currently being built: a marker from the future is a typo or a copy-paste from another field.
 * A marker equal to the version being built (e.g. 1.2.0 against 1.2.0-SNAPSHOT) is also expected
 * (in regular CI builds we build snapshot versions).
 *
 * @author Mikhail Polivakha
 */
object MarkersAreWellFormed : StatelessContractInvariant {

    override fun check(schema: ContractSchema): List<String> =
        schema.markedParts.flatMap { part ->
            listOf(INTRODUCED_IN, DEPRECATED_IN).mapNotNull { marker ->
                problemOf(schema, part, marker)
            }
        }

    private fun problemOf(
        schema: ContractSchema, part: ContractSchema.MarkedPart, marker: String): String? {

        val raw = schema.marker(part.node, marker) ?: return null
        val version = Version.parse(raw)
            ?: return "${part.location} has '$marker: $raw' that is not of the x.y.z form"

        // Markers carry no patch-level meaning, so only major.minor is compared.
        if (version.copy(patch = 0) > schema.axelixVersion.copy(patch = 0)) {
            return ("${part.location} has '$marker: $version' that is ahead of the version "
                + "currently being built (${schema.axelixVersion})")
        }
        return null
    }
}
