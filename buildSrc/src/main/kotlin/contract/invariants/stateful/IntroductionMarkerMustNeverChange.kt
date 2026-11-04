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
import contract.ContractSchema.Companion.INTRODUCED_IN

/**
 * The 'x-axelix-introduced-in' of a part the latest release already knows of is history: it
 * records when released Masters & starters first saw the part, and every window computation
 * builds on it. Rewriting it forward would re-open already elapsed windows, rewriting it
 * backward would prematurely close running ones, so it must never change at all.
 *
 * An absent or malformed marker on either side is not reported here: the stateless invariants
 * already do.
 *
 * @author Mikhail Polivakha
 */
object IntroductionMarkerMustNeverChange : StatefulContractInvariant {

    override fun check(baseline: ContractSchema?, current: ContractSchema?): List<String> {
        if (baseline == null || current == null) {
            // A schema born or removed in this release has no released introduction marker to rewrite.
            return emptyList()
        }

        val baselineProperties = baseline.properties.associateBy { property -> property.name }

        // Removed properties are a different check. This one only rejects a marker that changed
        // on a part the release already knows.
        val survivingParts: List<Triple<String, JsonNode, JsonNode>> =
            listOf(Triple(current.location, baseline.node, current.node)) +
                current.properties.mapNotNull { property ->
                    baselineProperties[property.name]
                        ?.let { released -> Triple(property.location, released.node, property.node) }
                }

        return survivingParts.mapNotNull { (location, baselineNode, currentNode) ->
            val baselineIntroduction = baseline.markerVersion(baselineNode, INTRODUCED_IN) ?: return@mapNotNull null
            val currentIntroduction = current.markerVersion(currentNode, INTRODUCED_IN) ?: return@mapNotNull null

            if (currentIntroduction != baselineIntroduction) {
                ("$location has '$INTRODUCED_IN: $currentIntroduction' but the released contract "
                    + "(${baseline.axelixVersion}) knows it as introduced in $baselineIntroduction: "
                    + "the introduction marker may never change")
            } else {
                null
            }
        }
    }
}
