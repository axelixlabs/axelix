package contract.invariants

import contract.ContractDocument

/**
 * A single micro-invariant on the evolution of a contract document between the latest release
 * and the working tree, as opposed to a [contract.invariants.stateless.StatelessContractInvariant] that judges the working tree
 * document alone.
 *
 * Called statefull, because it performs the check of invaraints between two versions of [ContractDocument], so
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
    fun check(baseline: ContractDocument?, current: ContractDocument): List<String>
}
