package contract.invariants.stateless

import contract.ContractDocument
import contract.ContractDocument.Companion.INTRODUCED_IN
import contract.ContractDocument.Companion.WINDOW_MINORS

/**
 * A property added to a starter-produced payload after the birth of the feature is not sent by
 * the older starters within the compatibility window, so the Master cannot rely on its presence
 * — the property cannot be 'required' — until the window has passed. Properties born with the
 * feature are obviously exempt, since such property will be in every starter that is aware about
 * the given opertaion at all.
 *
 * @author Mikhail Polivakha
 */
object RequiredHonoursTheWindow : StatelessContractInvariant {

    override fun check(document: ContractDocument): List<String> {
        val featureIntroduced = document.markerVersion(document.info, INTRODUCED_IN)
            ?: return emptyList()

        return document.properties.mapNotNull { property ->
            val introduced = document.markerVersion(property.node, INTRODUCED_IN)

            if (
                property.required &&
                // property is produced by the starter
                property.schemaName in document.starterProducedSchemas &&
                // was introduced later, after the operatio nexisted at all
                introduced != null && introduced > featureIntroduced &&
                // the slidning compatiblility window did not yet pass
                !document.windowPassed(introduced)
            ) {
                ("${property.location} cannot be 'required' yet: starters older than $introduced "
                    + "do not send it and only leave the compatibility window in "
                    + "${introduced.major}.${introduced.minor + WINDOW_MINORS}")
            } else {
                null
            }
        }
    }
}
