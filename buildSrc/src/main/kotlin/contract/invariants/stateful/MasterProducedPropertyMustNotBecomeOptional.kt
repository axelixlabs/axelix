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

    override fun check(baseline: ContractSchema?, current: ContractSchema?): List<String> {
        if (baseline == null || current == null) {
            return emptyList()
        }

        val baselineProperties = baseline.properties.associateBy { property -> property.name }

        return current.properties.mapNotNull { currentProperty ->
            val released = baselineProperties[currentProperty.name]
                ?: return@mapNotNull null

            if (released.required && !currentProperty.required && current.producedBy != "starter") {
                ("${currentProperty.location} is produced by the Master and is 'required' in the released "
                    + "contract (${baseline.axelixVersion}): withdrawing 'required' is a hidden removal, "
                    + "deprecate the property instead and keep it required until the window passes")
            } else {
                null
            }
        }
    }
}
