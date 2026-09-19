package contract

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.io.File
import org.gradle.api.GradleException

/**
 * A parsed contract of the single operation between Axelix Master & starters.
 *
 * @author Mikhail Polivakha
 */
class ContractDocument private constructor(private val root: JsonNode, val currentVersion: Version) {

    val info: JsonNode = root.path("info")

    val server: String = info.path(SERVER).asText("")

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
     */
    val starterProducedSchemas: Set<String> by lazy {
        if (server !in SERVER_SIDES) {
            return@lazy emptySet()
        }
        val produced = mutableSetOf<String>()
        root.path("paths").properties().forEach { (_, path) ->
            path.properties().forEach { (_, operation) ->
                val starterSide =
                    if (server == "starter") operation.path("responses") else operation.path("requestBody")
                starterSide.findValues("\$ref").forEach { reference ->
                    produced += reference.asText().substringAfterLast('/')
                }
            }
        }

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
        return@lazy produced
    }

    /**
     * The raw value of the marker, or null when it is absent.
     */
    fun marker(node: JsonNode, marker: String): String? =
        node.path(marker).takeUnless { value -> value.isMissingNode }?.asText()

    /**
     * The parsed value of the marker, or null when it is absent or malformed. A malformed marker
     * is only ever reported by [invariants.MarkersAreWellFormed], every other invariant silently
     * treats it as absent to not pile duplicated problems on a single typo.
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
        currentVersion.major > marker.major
            || (currentVersion.major == marker.major
                && currentVersion.minor - marker.minor >= WINDOW_MINORS)

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

        fun parse(document: File, currentVersion: String): ContractDocument =
            ContractDocument(
                YAMLMapper().readTree(document),
                Version.parse(currentVersion)
                    ?: throw GradleException(
                        "The project version '$currentVersion' is not of the expected x.y.z[-QUALIFIER] form"))
    }
}
