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
     * Verified that the contract documents aligns with the backward compabilitiby guarnatees.
     */
    fun validate(document: File, currentVersionString: String) {
        val currentVersion = parseVersion(currentVersionString)
            ?: throw GradleException(
                "The project version '$currentVersionString' is not of the expected x.y.z[-QUALIFIER] form")

        val root = YAMLMapper().readTree(document)
        val problems = mutableListOf<String>()

        val info = root.path("info")
        if (info.path(SERVER).asText("") !in SERVER_SIDES) {
            problems += "the 'info' block must declare '$SERVER' as one of $SERVER_SIDES"
        }
        validateMarkers(info, "the 'info' block", currentVersion, problems)

        root.path("components").path("schemas").properties().forEach { (schemaName, schema) ->
            schema.path("properties").properties().forEach { (propertyName, property) ->
                validateMarkers(property, "the property '$schemaName.$propertyName'", currentVersion, problems)
            }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                "The contract document ${document.name} is invalid:\n"
                    + problems.joinToString("\n") { problem -> "  - $problem" })
        }
    }

    private fun validateMarkers(
        node: JsonNode, location: String, current: Version, problems: MutableList<String>) {

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
    }

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

    private data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {

        override fun compareTo(other: Version): Int =
            compareValuesBy(this, other, Version::major, Version::minor, Version::patch)

        override fun toString(): String = "$major.$minor.$patch"
    }
}
