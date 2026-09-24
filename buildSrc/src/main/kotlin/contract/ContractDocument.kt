package contract

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.io.File
import org.gradle.api.GradleException

/**
 * A parsed contract of the single operation between Axelix Master & starters.
 *
 * @param axelixVersion version of Axelix, when the given document was actaul
 * @param root the [JsonNode] that represents the root of the YAML document to be inspected
 *
 * @author Mikhail Polivakha
 */
class ContractDocument private constructor(private val root: JsonNode, val axelixVersion: Version) {

    val info: JsonNode = root.path("info")

    /**
     * The declared 'x-axelix-server' side(s). Empty when the marker is absent or malformed.
     */
    val sides: Set<String> = parseSides(info.path(SERVER))

    /**
     * Every part of the document that carries lifecycle markers.
     */
    val markedParts: List<MarkedPart>

    val properties: List<Property>

    init {
        properties = root.path("components").path("schemas").properties().flatMap { (schemaName, schema) ->

            val requiredProperties = schema.path("required").map { name -> name.asText() }.toSet()

            schema.path("properties").properties().map { (propertyName, propertyNode) ->
                Property(schemaName, propertyName, propertyNode, propertyName in requiredProperties)
            }
        }
        markedParts = listOf(MarkedPart("the 'info' block", info)) +
            properties.map { property -> MarkedPart(property.location, property.node) }
    }

    /**
     * Every operation the document describes, e.g. "POST /actuator/axelix-loggers", regardless
     * of the one-operation rule the invariants enforce on top of this list.
     */
    val operations: List<String> = root.path("paths").properties().flatMap { (path, pathItem) ->
        pathItem.properties()
            .filter { (key, _) -> key in HTTP_METHODS }
            .map { (method, _) -> "${method.uppercase()} $path" }
    }

    /**
     * The names of the schemas that **travel in a payload produced by the starter**.
     *
     * 1. Responses when the starter answers the call
     * 2. Request bodies when starter queries Axelix Master does
     * 3. Every schema in the document when both sides are declared - an escape hatch for a wire
     *    shape genuinely written by either side (e.g. a shared token format).
     */
    val starterProducedSchemas: Set<String> by lazy { producedSchemas("starter") }

    /**
     * The names of the schemas that **travel in a payload produced by Master**.
     */
    val masterProducedSchemas: Set<String> by lazy { producedSchemas("master") }

    private fun producedSchemas(side: String): Set<String> {
        if (sides.isEmpty() || sides.any { declaredSide -> declaredSide !in SERVER_SIDES }) {
            return emptySet()
        }
        if (sides.size > 1) {
            return expandThroughSchemas(root.path("components").path("schemas")
                .properties().map { (name, _) -> name }.toSet())
        }
        val starterBearer = if (sides.single() == "starter") "responses" else "requestBody"
        val masterBearer = if (starterBearer == "responses") "requestBody" else "responses"
        val bearer = if (side == "starter") starterBearer else masterBearer

        val produced = mutableSetOf<String>()
        root.path("paths").properties().forEach { (_, path) ->
            path.properties().forEach { (_, operation) ->
                operation.path(bearer).findValues("\$ref").forEach { reference ->
                    produced += reference.asText().substringAfterLast('/')
                }
            }
        }
        return expandThroughSchemas(produced)
    }

    /**
     * Parses the `x-axelix-server` marker, accepting either a single scalar value or a YAML list.
     */
    private fun parseSides(node: JsonNode): Set<String> = when {
        node.isMissingNode -> emptySet()
        node.isArray -> node.map { element -> element.asText() }.toSet()
        else -> setOf(node.asText())
    }

    private fun expandThroughSchemas(seed: Set<String>): Set<String> {
        val produced = seed.toMutableSet()
        val schemas = root.path("components").path("schemas")
        val queue = ArrayDeque(produced)
        while (queue.isNotEmpty()) {
            schemas.path(queue.removeFirst()).findValues("\$ref").forEach { reference ->
                val name = reference.asText().substringAfterLast('/')
                if (produced.add(name)) {
                    queue += name
                }
            }
        }
        return produced
    }

    /**
     * The raw value of the marker, or null when it is absent.
     */
    fun marker(node: JsonNode, marker: String): String? =
        node.path(marker).takeUnless { value -> value.isMissingNode }?.asText()

    /**
     * The parsed value of the custom marker, or null when it is absent or malformed.
     */
    fun markerVersion(node: JsonNode, marker: String): Version? =
        marker(node, marker)?.let(Version::parse)

    /**
     * Whether the compatibility window opened by the marker has passed for the version currently
     * being built, so the action gated on the marker is legal.
     *
     * A greater major passes outright for now: cross-major starters are rejected altogether. That probably
     * will change in the future.
     */
    fun windowPassed(marker: Version): Boolean =
        axelixVersion.major > marker.major
            || (axelixVersion.major == marker.major
                && axelixVersion.minor - marker.minor >= WINDOW_MINORS)

    data class MarkedPart(val location: String, val node: JsonNode)

    data class Property(
        val schemaName: String, val name: String, val node: JsonNode, val required: Boolean) {

        val location: String = "the property '$schemaName.$name'"
    }

    companion object {

        const val INTRODUCED_IN = "x-axelix-introduced-in"
        const val DEPRECATED_IN = "x-axelix-deprecated-in"
        const val SERVER = "x-axelix-server"

        val SERVER_SIDES = setOf("starter", "master")

        private val HTTP_METHODS =
            setOf("get", "put", "post", "delete", "options", "head", "patch", "trace")

        /**
         * The compatibility window is 4 minors including the current one, so an action gated on
         * a marker becomes legal once the current minor is ahead of the marker by this much.
         */
        const val WINDOW_MINORS = 3

        fun parse(document: File, currentAxelixVersion: String): ContractDocument =
            of(YAMLMapper().readTree(document), currentAxelixVersion)

        fun parse(document: ByteArray, currentAxelixVersion: Version): ContractDocument =
            ContractDocument(YAMLMapper().readTree(document), currentAxelixVersion)

        private fun of(root: JsonNode, currentAxelixVersion: String): ContractDocument =
            ContractDocument(
                root,
                Version.parse(currentAxelixVersion)
                    ?: throw GradleException(
                        "The project version '$currentAxelixVersion' is not of the expected x.y.z[-QUALIFIER] form"))
    }
}
