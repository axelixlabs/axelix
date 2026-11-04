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

/**
 * A contract document is the unit of versioning, so it must not describe more than one
 * operation. Describing none is the normal case: the document defines a payload and carries
 * no path. A version of a document holding several operations cannot honestly represent a
 * change to just one of them.
 *
 * @author Mikhail Polivakha
 */
object DocumentDescribesOneOperation : StatelessContractInvariant {

    override fun check(document: ContractDocument): List<String> =
        if (document.operations.size > 1) {
            listOf("the document must describe at most one operation, but describes "
                + "${document.operations.size}: ${document.operations.joinToString(", ")}")
        } else {
            emptyList()
        }
}
