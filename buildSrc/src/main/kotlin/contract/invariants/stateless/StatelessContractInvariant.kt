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

/**
 * A single micro-invariant of a contract schema that must be true.
 * It is called "stateless" because it checks the invariants within the current version
 * of the schema as it is in isolation. Its execution is, by nature, idempotent.
 *
 * @see contract.invariants.stateful.StatefulContractInvariant
 * @author Mikhail Polivakha
 */
fun interface StatelessContractInvariant {

    /**
     * Returns the violations of this invariant in the given schema, empty when invariant holds.
     */
    fun check(schema: ContractSchema): List<String>
}
