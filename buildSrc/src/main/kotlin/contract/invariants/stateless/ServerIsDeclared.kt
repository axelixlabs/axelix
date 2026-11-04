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
import contract.ContractDocument.Companion.BOTH
import contract.ContractDocument.Companion.SERVER
import contract.ContractDocument.Companion.SERVER_SIDES

/**
 * The document must declare which side answers the call: the producer of every payload — and
 * with it the applicable evolution rules — is derived from it.
 *
 * @author Mikhail Polivakha
 */
object ServerIsDeclared : StatelessContractInvariant {

    override fun check(document: ContractDocument): List<String> {
        if (document.server !in SERVER_SIDES) {
            return listOf("the 'info' block must declare '$SERVER' as one of $SERVER_SIDES")
        }
        if (document.server == BOTH && !document.schemaOnly) {
            return listOf(
                "'$SERVER: $BOTH' is only valid for a schema-only document that omits 'paths'")
        }
        return emptyList()
    }
}
