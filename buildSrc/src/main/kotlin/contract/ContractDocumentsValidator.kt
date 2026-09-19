package contract

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.io.File
import org.gradle.api.GradleException

/**
 * Validation of the Axelix Master <-> starter OpenAPI contract documents
 *
 * @author Mikhail Polivakha
 */
object ContractDocumentsValidator {

    private const val INTRODUCED_IN = "x-axelix-introduced-in"
    private const val DEPRECATED_IN = "x-axelix-deprecated-in"
    private const val SERVER = "x-axelix-server"

    private val SERVER_SIDES = setOf("starter", "master")
    private val VERSION_FORMAT = Regex("""(\d+)\.(\d+)\.(\d+)""")

    /**
     * The compatibility window is 4 minors including the current one, so an action gated on a
     * marker becomes legal once the current minor is ahead of the marker by this much.
     */
    private const val WINDOW_MINORS = 3

    /**
     * Verified that the contract documents aligns with the backward compabilitiby guarnatees.
     */
    fun validate(document: File, currentVersionString: String) {
        val currentVersion = parseVersion(currentVersionString)
            ?: throw GradleException(
                "The project version '$currentVersionString' is not of the expected x.y.z[-QUALIFIER] form")

        val root = YAMLMapper().readTree(document)
        val problems = mutableListOf<String>()

        val info = root.path("info")
        val server = info.path(SERVER).asText("")
        if (server !in SERVER_SIDES) {
            problems += "the 'info' block must declare '$SERVER' as one of $SERVER_SIDES"
        }
        val infoMarkers = validateMarkers(info, "the 'info' block", currentVersion, problems)

        if (infoMarkers.deprecated != null && windowPassed(infoMarkers.deprecated, currentVersion)) {
            problems += ("the operation was deprecated in ${infoMarkers.deprecated} and the "
                + "compatibility window has passed: remove the document")
        }

        val starterProduced = if (server in SERVER_SIDES) starterProducedSchemas(root, server) else emptySet()

        root.path("components").path("schemas").properties().forEach { (schemaName, schema) ->
            val required = schema.path("required").map { name -> name.asText() }.toSet()

            schema.path("properties").properties().forEach { (propertyName, property) ->
                val location = "the property '$schemaName.$propertyName'"
                val markers = validateMarkers(property, location, currentVersion, problems)

                // A property added to a starter-produced payload after the birth of the feature
                // is not sent by the older starters within the compatibility window, so the
                // Master cannot rely on its presence until the window has passed.
                if (schemaName in starterProduced && propertyName in required
                    && markers.introduced != null && infoMarkers.introduced != null
                    && markers.introduced > infoMarkers.introduced
                    && !windowPassed(markers.introduced, currentVersion)) {
                    problems += ("$location cannot be 'required' yet: starters older than "
                        + "${markers.introduced} do not send it and only leave the compatibility "
                        + "window in ${markers.introduced.major}.${markers.introduced.minor + WINDOW_MINORS}")
                }

                if (markers.deprecated != null && windowPassed(markers.deprecated, currentVersion)) {
                    problems += ("$location was deprecated in ${markers.deprecated} and the "
                        + "compatibility window has passed: remove it from the contract")
                }
            }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                "The contract document ${document.name} is invalid:\n"
                    + problems.joinToString("\n") { problem -> "  - $problem" })
        }
    }

    private fun validateMarkers(
        node: JsonNode, location: String, current: Version, problems: MutableList<String>): Markers {

        // both on the top-level 'info' block and on per-property level blocks we must have the marker
        // when the property was introduced.
        if (node.path(INTRODUCED_IN).isMissingNode) {
            problems += "$location is missing '$INTRODUCED_IN'"
        }

        val introduced = validateVersionMarker(node, INTRODUCED_IN, location, current, problems)
        val deprecated = validateVersionMarker(node, DEPRECATED_IN, location, current, problems)

        // if property is deprecated, it must be deprecated strictly AFTER it was introduced. Otherwise it does not make sense.
        if (introduced != null && deprecated != null && deprecated <= introduced) {
            problems += ("$location has '$DEPRECATED_IN: $deprecated' that is not strictly later "
                + "than '$INTRODUCED_IN: $introduced'")
        }
        return Markers(introduced, deprecated)
    }

    /**
     * The names of the schemas that travel in a payload produced by the starter: the responses
     * when the starter answers the call, the request bodies when the Master does. Schemas
     * referenced by the properties of a produced schema travel in the same payload.
     */
    private fun starterProducedSchemas(root: JsonNode, server: String): Set<String> {
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
        return produced
    }

    private fun windowPassed(marker: Version, current: Version): Boolean =
        current.major > marker.major
            || (current.major == marker.major && current.minor - marker.minor >= WINDOW_MINORS)

    /**
     * Returns the parsed marker, or null when it is absent or malformed (the latter is reported).
     */
    private fun validateVersionMarker(
        node: JsonNode, marker: String, location: String, current: Version,
        problems: MutableList<String>): Version? {

        val value = node.path(marker)
        if (value.isMissingNode) {
            return null
        }

        val version = parseVersion(value.asText())
        if (version == null) {
            problems += "$location has '$marker: ${value.asText()}' that is not of the x.y.z form"
            return null
        }

        // Make sure that version that is specified in the yaml contract is not in the future.
        // Version must be either smaller (already released), or eausl to current (unreleased version)
        if (version.copy(patch = 0) > current.copy(patch = 0)) {
            problems += ("$location has '$marker: $version' that is ahead of the version "
                + "currently being built ($current)")
        }
        return version
    }

    private fun parseVersion(value: String): Version? {
        val match = VERSION_FORMAT.matchAt(value, 0) ?: return null
        val (major, minor, patch) = match.destructured
        return Version(major.toInt(), minor.toInt(), patch.toInt())
    }

    private data class Markers(val introduced: Version?, val deprecated: Version?)

    private data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {

        override fun compareTo(other: Version): Int =
            compareValuesBy(this, other, Version::major, Version::minor, Version::patch)

        override fun toString(): String = "$major.$minor.$patch"
    }
}
