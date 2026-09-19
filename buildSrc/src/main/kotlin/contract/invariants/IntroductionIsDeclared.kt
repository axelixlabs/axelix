package contract.invariants

import contract.ContractDocument
import contract.ContractDocument.Companion.INTRODUCED_IN

/**
 * The 'info' block and EVERY property must declare the Axelix version they were introduced in.
 * The per-property version is what answers "may the Master already rely on this property / drop
 * the support of it", so a property without it is a bug waiting to happen — there is
 * deliberately no fallback to a document-level default.
 *
 * @author Mikhail Polivakha
 */
object IntroductionIsDeclared : ContractInvariant {

    override fun check(document: ContractDocument): List<String> =
        document.markedParts
            .filter { part -> document.marker(part.node, INTRODUCED_IN) == null }
            .map { part -> "${part.location} is missing '$INTRODUCED_IN'" }
}
