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

import contract.ContractDocument
import contract.ContractDocument.Companion.DEPRECATED_IN

/**
 * Once the compatibility window of a deprecation has passed, keeping the deprecated part of the
 * contract around is forbidden: this check is needed for us to ensure that we do not carry the
 * stuff that we can omit.
 *
 * @author Mikhail Polivakha
 */
object DeprecatedIsRemovedAfterTheWindow : StatelessContractInvariant {

    override fun check(document: ContractDocument): List<String> {
        val problems = mutableListOf<String>()

        document.schemas.forEach { schema ->
            val deprecated = document.markerVersion(schema.node, DEPRECATED_IN)
            if (deprecated != null && document.windowPassed(deprecated)) {
                problems += ("${schema.location} was deprecated in $deprecated and the "
                    + "compatibility window has passed: remove it from the contract")
            }
        }

        document.properties.forEach { property ->
            val deprecated = document.markerVersion(property.node, DEPRECATED_IN)
            if (deprecated != null && document.windowPassed(deprecated)) {
                problems += ("${property.location} was deprecated in $deprecated and the "
                    + "compatibility window has passed: remove it from the contract")
            }
        }
        return problems
    }
}
