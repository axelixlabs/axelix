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

/**
 * A property the latest release already knows of must keep its wire shape: changing 'type' or
 * 'format' under the same name silently breaks every released Master & starter that still
 * parses the old shape, and no compatibility window can make it safe. The regulation therefore
 * knows no in-place redefinition at all: a field update is always a new property (new name)
 * plus a deprecation of the old one.
 *
 * @author Mikhail Polivakha
 */
object PropertyTypeAndFormatMustNeverChange : StatefulContractInvariant {

    override fun check(baseline: List<ContractSchema>?, current: List<ContractSchema>): List<String> {
        if (baseline == null) {
            // a document born now redefines nothing: no released consumer has ever seen it
            return emptyList()
        }

        val baselineProperties = baseline.flatMap { schema -> schema.properties }
            .associateBy { property -> property.schemaName to property.name }

        return current.flatMap { schema -> schema.properties }.flatMap { property ->
            val released = baselineProperties[property.schemaName to property.name]
                ?: return@flatMap emptyList<String>()

            listOfNotNull(
                redefinition(property.location, "type", released.node, property.node, baseline.first()),
                redefinition(property.location, "format", released.node, property.node, baseline.first()))
        }
    }

    private fun redefinition(
        location: String,
        field: String,
        baselineNode: JsonNode,
        currentNode: JsonNode,
        baseline: ContractSchema): String? {

        val releasedValue = declared(baselineNode, field)
        val currentValue = declared(currentNode, field)

        if (releasedValue == currentValue) {
            return null
        }
        return ("$location changed its '$field' from ${quoted(releasedValue)} to ${quoted(currentValue)} "
            + "against the released contract (${baseline.axelixVersion}): a released property must never "
            + "be redefined in place, introduce a new property and deprecate the old one instead")
    }

    private fun declared(node: JsonNode, field: String): String? =
        node.path(field).takeUnless { value -> value.isMissingNode }?.asText()

    private fun quoted(value: String?): String =
        value?.let { "'$it'" } ?: "nothing"
}
