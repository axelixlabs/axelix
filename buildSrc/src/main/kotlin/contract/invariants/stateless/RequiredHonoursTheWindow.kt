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
import contract.ContractSchema.Companion.INTRODUCED_IN
import contract.ContractSchema.Companion.WINDOW_MINORS

/**
 * A property added to a starter-produced schema after the schema was born is not sent by the
 * older starters within the compatibility window, so the Master cannot rely on its presence
 * — the property cannot be 'required' — until the window has passed. Properties born with the
 * schema are exempt, since a starter that knows the schema at all sends them.
 *
 * @author Mikhail Polivakha
 */
object RequiredHonoursTheWindow : StatelessContractInvariant {

    override fun check(schema: ContractSchema): List<String> {
        val bornWith = schema.markerVersion(schema.node, INTRODUCED_IN)

        return schema.properties.mapNotNull { property ->
            val introduced = schema.markerVersion(property.node, INTRODUCED_IN)

            if (
                property.required &&
                schema.producedBy == "starter" &&
                introduced != null && bornWith != null && introduced > bornWith &&
                !schema.windowPassed(introduced)
            ) {
                ("${property.location} cannot be 'required' yet: starters older than $introduced "
                    + "do not send it and only leave the compatibility window in "
                    + "${introduced.major}.${introduced.minor + WINDOW_MINORS}")
            } else {
                null
            }
        }
    }
}
