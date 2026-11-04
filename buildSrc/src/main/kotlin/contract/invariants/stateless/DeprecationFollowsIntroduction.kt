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

/**
 * A deprecation must be strictly later than the introduction: deprecating a part of the
 * contract in the very release that introduced it means it should not have been introduced
 * at all.
 *
 * @author Mikhail Polivakha
 */
object DeprecationFollowsIntroduction : StatelessContractInvariant {

    override fun check(schema: ContractSchema): List<String> =
        schema.markedParts.mapNotNull { part ->
            val introduced = schema.markerVersion(part.node, INTRODUCED_IN)
            val deprecated = schema.markerVersion(part.node, DEPRECATED_IN)

            if (introduced != null && deprecated != null && deprecated <= introduced) {
                ("${part.location} has '$DEPRECATED_IN: $deprecated' that is not strictly later "
                    + "than '$INTRODUCED_IN: $introduced'")
            } else {
                null
            }
        }
}
