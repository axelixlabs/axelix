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

/**
 * A 'required' property of a master-produced payload is a promise every released starter builds
 * on: the Master always sends it. Withdrawing 'required' is therefore a hidden partial removal —
 * the Master reserves the right to stop sending the property while starters within the
 * compatibility window still rely on its presence. The regulation knows only one way to stop
 * sending a master-produced property: deprecate it, keep sending it, and remove it once the
 * window has passed.
 *
 * Starter-produced payloads are free to weaken 'required': the Master is never older than the
 * starter it talks to, so its model already tolerates the absence.
 *
 * @author Mikhail Polivakha
 */
object MasterProducedPropertyMustNotBecomeOptional : StatefulContractInvariant {

    override fun check(baseline: List<ContractSchema>?, current: List<ContractSchema>): List<String> {
        if (baseline == null) {
            // a document born now promised nothing: no released starter relies on its properties
            return emptyList()
        }

        val baselineProperties = baseline.flatMap { schema -> schema.properties }
            .associateBy { property -> property.schemaName to property.name }
        val starterProducedSchemas = current.filter { schema -> schema.producedBy == "starter" }
            .map { schema -> schema.name }.toSet()

        return current.flatMap { schema -> schema.properties }.mapNotNull { currentProperty ->
            val released = baselineProperties[currentProperty.schemaName to currentProperty.name]
                ?: return@mapNotNull null

            if (released.required && !currentProperty.required
                && currentProperty.schemaName !in starterProducedSchemas) {
                ("${currentProperty.location} is produced by the Master and is 'required' in the released "
                    + "contract (${baseline.first().axelixVersion}): withdrawing 'required' is a hidden removal, "
                    + "deprecate the property instead and keep it required until the window passes")
            } else {
                null
            }
        }
    }
}
