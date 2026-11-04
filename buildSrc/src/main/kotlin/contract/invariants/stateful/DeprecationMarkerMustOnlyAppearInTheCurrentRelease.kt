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

import com.fasterxml.jackson.databind.JsonNode
import contract.ContractSchema
import contract.ContractSchema.Companion.DEPRECATED_IN

/**
 * Stateful checks for the deprecation marker changes.
 *
 * 1. If deprecation appears in the upcoing release, it must have the correct version - the upcoling verison
 *    of Axelix.
 * 2. Deprecation must never change. If we deprecated in version X.Y.Z, we must not change
 *    the deprecation version into X.Y.Z + 1;
 *
 * An absent or malformed marker is otherwise not reported here: the stateless invariants
 * already do.
 *
 * @author Mikhail Polivakha
 */
object DeprecationMarkerMustOnlyAppearInTheCurrentRelease : StatefulContractInvariant {

    override fun check(baseline: List<ContractSchema>?, current: List<ContractSchema>): List<String> {
        if (baseline == null) {
            // a deprecation cannot appear, change or vanish against a document that did not
            // exist; a deprecation born together with the document is rejected statelessly,
            // because it cannot be strictly later than the introduction
            return emptyList()
        }

        val baselineSchemas = baseline.associateBy { schema -> schema.name }
        val baselineProperties = baseline.flatMap { schema -> schema.properties }
            .associateBy { property -> property.schemaName to property.name }

        val survivingParts: List<Triple<String, JsonNode, JsonNode>> =
            current.mapNotNull { schema ->
                baselineSchemas[schema.name]
                    ?.let { released -> Triple(schema.location, released.node, schema.node) }
            } +
                current.flatMap { schema -> schema.properties }.mapNotNull { property ->
                    baselineProperties[property.schemaName to property.name]
                        ?.let { released -> Triple(property.location, released.node, property.node) }
                }

        return survivingParts.mapNotNull { (location, baselineNode, currentNode) ->
            val baselineDeprecation = baseline.first().markerVersion(baselineNode, DEPRECATED_IN)
            val currentDeprecation = current.first().markerVersion(currentNode, DEPRECATED_IN)

            when {

                // property is supposed to be deprecated the upcoming release, but the wrong version was specified
                baselineDeprecation == null && currentDeprecation != null && currentDeprecation != current.first().axelixVersion ->

                    ("$location has '$DEPRECATED_IN: $currentDeprecation' that the released contract "
                        + "(${baseline.first().axelixVersion}) does not: a deprecation that is supposed to appear in the upcoming release must "
                        + "specify the version currently being built (${current.first().axelixVersion})")

                // property was deprecated already, but the changed the version in which it was depreacted - it is forbidden
                baselineDeprecation != null && currentDeprecation != null && currentDeprecation != baselineDeprecation ->
                    ("$location has '$DEPRECATED_IN: $currentDeprecation' but the released contract "
                        + "(${baseline.first().axelixVersion}) already deprecates it in $baselineDeprecation: "
                        + "the deprecation marker must never change")

                else -> null
            }
        }
    }
}
