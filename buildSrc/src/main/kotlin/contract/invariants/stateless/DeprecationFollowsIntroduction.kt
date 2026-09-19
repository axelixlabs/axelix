package contract.invariants.stateless

import contract.ContractDocument
import contract.ContractDocument.Companion.DEPRECATED_IN
import contract.ContractDocument.Companion.INTRODUCED_IN

/**
 * A deprecation must be strictly later than the introduction: deprecating a part of the
 * contract in the very release that introduced it means it should not have been introduced
 * at all.
 *
 * @author Mikhail Polivakha
 */
object DeprecationFollowsIntroduction : StatelessContractInvariant {

    override fun check(document: ContractDocument): List<String> =
        document.markedParts.mapNotNull { part ->
            val introduced = document.markerVersion(part.node, INTRODUCED_IN)
            val deprecated = document.markerVersion(part.node, DEPRECATED_IN)

            if (introduced != null && deprecated != null && deprecated <= introduced) {
                ("${part.location} has '$DEPRECATED_IN: $deprecated' that is not strictly later "
                    + "than '$INTRODUCED_IN: $introduced'")
            } else {
                null
            }
        }
}
