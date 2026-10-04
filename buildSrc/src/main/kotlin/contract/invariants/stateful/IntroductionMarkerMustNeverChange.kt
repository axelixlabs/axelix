package contract.invariants.stateful

import com.fasterxml.jackson.databind.JsonNode
import contract.ContractDocument
import contract.ContractDocument.Companion.INTRODUCED_IN

/**
 * The 'x-axelix-introduced-in' of a part the latest release already knows of is history: it
 * records when released Masters & starters first saw the part, and every window computation
 * builds on it. Rewriting it forward would re-open already elapsed windows, rewriting it
 * backward would prematurely close running ones, so it must never change at all.
 *
 * An absent or malformed marker on either side is not reported here: the stateless invariants
 * already do.
 *
 * @author Mikhail Polivakha
 */
object IntroductionMarkerMustNeverChange : StatefulContractInvariant {

    override fun check(baseline: ContractDocument?, current: ContractDocument): List<String> {
        if (baseline == null) {
            // no contract in previous version means that x-axelix-introduced-in cannot possibly be
            // mutated - there is nothing to mutate, there were no previous version
            return emptyList()
        }

        val baselineProperties = baseline.properties
            .associateBy { property -> property.schemaName to property.name }

        // note that this does not incldue the removed properties or removed operation - that is
        // a whole different story. This invaraint just ensures that introduciton marker never CHANGES
        val survivingParts: List<Triple<String, JsonNode, JsonNode>> =
            listOf(Triple("the 'info' block", baseline.info, current.info)) +
                current.properties.mapNotNull { property ->
                    baselineProperties[property.schemaName to property.name]
                        ?.let { baseline -> Triple(property.location, baseline.node, property.node) }
                }

        return survivingParts.mapNotNull { (location, baselineNode, currentNode) ->
            val baselineIntroduction = baseline.markerVersion(baselineNode, INTRODUCED_IN) ?: return@mapNotNull null
            val currentIntroduction = current.markerVersion(currentNode, INTRODUCED_IN) ?: return@mapNotNull null

            if (currentIntroduction != baselineIntroduction) {
                ("$location has '$INTRODUCED_IN: $currentIntroduction' but the released contract "
                    + "(${baseline.axelixVersion}) knows it as introduced in $baselineIntroduction: "
                    + "the introduction marker may never change")
            } else {
                null
            }
        }
    }
}
