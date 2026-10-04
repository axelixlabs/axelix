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

        val operationDeprecated = document.markerVersion(document.info, DEPRECATED_IN)
        if (operationDeprecated != null && document.windowPassed(operationDeprecated)) {
            problems += ("the operation was deprecated in $operationDeprecated and the "
                + "compatibility window has passed: remove the document")
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
