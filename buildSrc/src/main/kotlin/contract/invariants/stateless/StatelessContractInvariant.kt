package contract.invariants.stateless

import contract.ContractDocument

/**
 * A single micro-invariant of a contract document that must be true.
 * It is called "stateless" because it checks the invariants within the current version
 * of the contract as it is in isolation. It's execution is, by nature, idempotent.
 *
 * @see contract.invariants.stateful.StatefulContractInvariant
 * @author Mikhail Polivakha
 */
fun interface StatelessContractInvariant {

    /**
     * Returns the violations of this invariant in the given document, empty when invariant holds.
     */
    fun check(document: ContractDocument): List<String>
}
