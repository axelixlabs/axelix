package contract.invariants

import contract.ContractDocument

/**
 * A single micro-invariant of a contract document that must be true.
 *
 * @author Mikhail Polivakha
 */
fun interface ContractInvariant {

    /**
     * Returns the violations of this invariant in the given document, empty when invariant holds.
     */
    fun check(document: ContractDocument): List<String>
}
