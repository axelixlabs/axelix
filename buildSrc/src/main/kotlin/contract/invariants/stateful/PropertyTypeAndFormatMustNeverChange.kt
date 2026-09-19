package contract.invariants.stateful

import com.fasterxml.jackson.databind.JsonNode
import contract.ContractDocument

/**
 * A property the latest release already knows of must keep its wire shape: changing 'type' or
 * 'format' under the same name silently breaks every released Master & starter that still
 * parses the old shape, and no compatibility window can make it safe. The regulation therefore
 * knows no in-place redefinition at all: a field update is always a new property (new name)
 * plus a deprecation of the old one.
 *
 * @author Mikhail Polivakha
 */
object PropertyTypeAndFormatMustNeverChange : StatefulContractInvariant {

    override fun check(baseline: ContractDocument?, current: ContractDocument): List<String> {
        if (baseline == null) {
            // a document born now redefines nothing: no released consumer has ever seen it
            return emptyList()
        }

        val baselineProperties = baseline.properties
            .associateBy { property -> property.schemaName to property.name }

        return current.properties.flatMap { property ->
            val released = baselineProperties[property.schemaName to property.name]
                ?: return@flatMap emptyList<String>()

            listOfNotNull(
                redefinition(property.location, "type", released.node, property.node, baseline),
                redefinition(property.location, "format", released.node, property.node, baseline))
        }
    }

    private fun redefinition(
        location: String,
        field: String,
        baselineNode: JsonNode,
        currentNode: JsonNode,
        baseline: ContractDocument): String? {

        val releasedValue = declared(baselineNode, field)
        val currentValue = declared(currentNode, field)

        if (releasedValue == currentValue) {
            return null
        }
        return ("$location changed its '$field' from ${quoted(releasedValue)} to ${quoted(currentValue)} "
            + "against the released contract (${baseline.axelixVersion}): a released property must never "
            + "be redefined in place, introduce a new property and deprecate the old one instead")
    }

    private fun declared(node: JsonNode, field: String): String? =
        node.path(field).takeUnless { value -> value.isMissingNode }?.asText()

    private fun quoted(value: String?): String =
        value?.let { "'$it'" } ?: "nothing"
}
