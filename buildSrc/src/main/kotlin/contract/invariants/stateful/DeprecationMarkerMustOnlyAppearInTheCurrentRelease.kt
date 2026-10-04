package contract.invariants.stateful

import com.fasterxml.jackson.databind.JsonNode
import contract.ContractDocument
import contract.ContractDocument.Companion.DEPRECATED_IN

/**
 * Stateful checks for the deprecation marker changes.
 *
 * 1. If deprecation appears in the upcoing release, it must have the correct version - the upcoling verison
 *    of Axelix.
 * 2. Deprecation must never change. If we deprecated in version X.Y.Z, we must not change
 *    the deprecation version into X.Y.Z + 1;
 *
 * An absent or malformed marker is otherwise not reported here: the stateless invariants
 * already do.
 *
 * @author Mikhail Polivakha
 */
object DeprecationMarkerMustOnlyAppearInTheCurrentRelease : StatefulContractInvariant {

    override fun check(baseline: ContractDocument?, current: ContractDocument): List<String> {
        if (baseline == null) {
            // a deprecation cannot appear, change or vanish against a document that did not
            // exist; a deprecation born together with the document is rejected statelessly,
            // because it cannot be strictly later than the introduction
            return emptyList()
        }

        val baselineProperties = baseline.properties
            .associateBy { property -> property.schemaName to property.name }

        val survivingParts: List<Triple<String, JsonNode, JsonNode>> =
            listOf(Triple("the 'info' block", baseline.info, current.info)) +
                current.properties.mapNotNull { property ->
                    baselineProperties[property.schemaName to property.name]
                        ?.let { released -> Triple(property.location, released.node, property.node) }
                }

        return survivingParts.mapNotNull { (location, baselineNode, currentNode) ->
            val baselineDeprecation = baseline.markerVersion(baselineNode, DEPRECATED_IN)
            val currentDeprecation = current.markerVersion(currentNode, DEPRECATED_IN)

            when {

                // property is supposed to be deprecated the upcoming release, but the wrong version was specified
                baselineDeprecation == null && currentDeprecation != null && currentDeprecation != current.axelixVersion ->

                    ("$location has '$DEPRECATED_IN: $currentDeprecation' that the released contract "
                        + "(${baseline.axelixVersion}) does not: a deprecation that is supposed to appear in the upcoming release must "
                        + "specify the version currently being built (${current.axelixVersion})")

                // property was deprecated already, but the changed the version in which it was depreacted - it is forbidden
                baselineDeprecation != null && currentDeprecation != null && currentDeprecation != baselineDeprecation ->
                    ("$location has '$DEPRECATED_IN: $currentDeprecation' but the released contract "
                        + "(${baseline.axelixVersion}) already deprecates it in $baselineDeprecation: "
                        + "the deprecation marker must never change")

                else -> null
            }
        }
    }
}
