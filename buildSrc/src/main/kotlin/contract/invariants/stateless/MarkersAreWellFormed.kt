package contract.invariants.stateless

import contract.ContractDocument
import contract.ContractDocument.Companion.DEPRECATED_IN
import contract.ContractDocument.Companion.INTRODUCED_IN
import contract.Version

/**
 * Every lifecycle marker must be of the x.y.z form and must not be ahead of the version
 * currently being built: a marker from the future is a typo or a copy-paste from another field.
 * A marker equal to the version being built (e.g. 1.2.0 against 1.2.0-SNAPSHOT) is also expected
 * (in regular CI builds we build snapshot versions).
 *
 * @author Mikhail Polivakha
 */
object MarkersAreWellFormed : StatelessContractInvariant {

    override fun check(document: ContractDocument): List<String> =
        document.markedParts.flatMap { part ->
            listOf(INTRODUCED_IN, DEPRECATED_IN).mapNotNull { marker ->
                problemOf(document, part, marker)
            }
        }

    private fun problemOf(
        document: ContractDocument, part: ContractDocument.MarkedPart, marker: String): String? {

        val raw = document.marker(part.node, marker) ?: return null
        val version = Version.parse(raw)
            ?: return "${part.location} has '$marker: $raw' that is not of the x.y.z form"

        // Markers carry no patch-level meaning, so only major.minor is compared.
        if (version.copy(patch = 0) > document.axelixVersion.copy(patch = 0)) {
            return ("${part.location} has '$marker: $version' that is ahead of the version "
                + "currently being built (${document.axelixVersion})")
        }
        return null
    }
}
