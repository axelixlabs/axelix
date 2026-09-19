package contract.invariants.stateful

import contract.ContractDocument
import contract.ContractDocument.Companion.INTRODUCED_IN

/**
 * A part of the contract the latest release does not know of — the whole document or a single
 * property — is being born right now, so its 'x-axelix-introduced-in' must name the version
 * currently being built. A backdated marker would immediately mislead the window arithmetic:
 * the part would look like an old promise that released Masters & starters already rely on,
 * while in reality nothing released has ever seen it.
 *
 * An absent or malformed marker is not reported here: the stateless invariants already do.
 *
 * @author Mikhail Polivakha
 */
object NewPartsMustBeMarkedAsIntroducedInTheCurrentRelease : StatefulContractInvariant {

    override fun check(baseline: ContractDocument?, current: ContractDocument): List<String> {
        val unreleasedMarkers = if (baseline == null) {
            // all the properties are supposed to be checked, since absense of the baseline
            // means all the properties are new
            current.markedParts
        } else {
            val released = baseline.properties.map { property -> property.schemaName to property.name }.toSet()
            current.properties
                // taking only unreleased properties
                .filter { property -> (property.schemaName to property.name) !in released }
                .map { property -> ContractDocument.MarkedPart(property.location, property.node) }
        }

        return unreleasedMarkers.mapNotNull { part ->
            val introduced = current.markerVersion(part.node, INTRODUCED_IN) ?: return@mapNotNull null

            if (introduced != current.axelixVersion) {
                ("${part.location} is born in this release but has '$INTRODUCED_IN: $introduced' "
                    + "instead of the version currently being built (${current.axelixVersion})")
            } else {
                null
            }
        }
    }
}
