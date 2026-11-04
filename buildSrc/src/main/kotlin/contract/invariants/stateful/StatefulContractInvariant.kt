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
 * A single micro-invariant on the evolution of a contract document between the latest release
 * and the working tree, as opposed to a [contract.invariants.stateless.StatelessContractInvariant] that judges the working tree
 * document alone.
 *
 * Called statefull, because it performs the check of invaraints between two versions of [contract.ContractSchema], so
 * it needs the baseline for comaparison.
 *
 * @author Mikhail Polivakha
 */
fun interface StatefulContractInvariant {

    /**
     * Returns the violations of this invariant in the given pair of documents, empty when the
     * invariant holds. The [baseline] is null when the document did not exist at the latest
     * release, i.e. the operation is being born in the release currently being developed.
     */
    fun check(baseline: List<ContractSchema>?, current: List<ContractSchema>): List<String>
}