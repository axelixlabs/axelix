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
 * A single micro-invariant on the evolution of a schema between the latest release and the
 * working tree, as opposed to a [contract.invariants.stateless.StatelessContractInvariant] that
 * judges the working tree schema alone.
 *
 * @author Mikhail Polivakha
 */
fun interface StatefulContractInvariant {

    /**
     * Returns the violations of this invariant for the given schema pair, empty when the
     * invariant holds. [baseline] is null for a schema introduced after the latest release;
     * [current] is null for a schema removed from the working tree. At least one is non-null.
     */
    fun check(baseline: ContractSchema?, current: ContractSchema?): List<String>
}
