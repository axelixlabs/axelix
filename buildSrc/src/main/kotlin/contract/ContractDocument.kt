/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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

    val server: String = info.path(SERVER).asText("")

    /**
     * OpenAPI 3.1 does not require `paths`. A document that omits the key entirely describes a
     * payload rather than an HTTP operation — the access token is such a document.
     */
    val schemaOnly: Boolean = !root.has("paths")

    val schemaNames: Set<String> =
        root.path("components").path("schemas").properties().map { (name, _) -> name }.toSet()

    /**
     * Every part of the document that carries lifecycle markers.
     */
    val markedParts: List<MarkedPart>

    val properties: List<Property>

    init {
        properties = root.path("components").path("schemas").properties().flatMap { (schemaName, schema) ->

            // The inline 'allOf' parts (e.g. subtypes) declare their own properties.
            (listOf(schema) + schema.path("allOf").toList()).flatMap { part ->
                val requiredProperties = part.path("required").map { name -> name.asText() }.toSet()

                part.path("properties").properties().map { (propertyName, propertyNode) ->
                    Property(schemaName, propertyName, propertyNode, propertyName in requiredProperties)
                }
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
        // A payload minted by both sides is starter-produced and master-produced at once.
        if (server == BOTH) {
            return@lazy schemaNames
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
            val schema = schemas.path(queue.removeFirst())

            // Discriminator mapping holds the subtype references as plain values, not as '$ref'.
            val references =
                schema.findValues("\$ref") + schema.path("discriminator").path("mapping").toList()

            references.forEach { reference ->
                val name = reference.asText().substringAfterLast('/')
                if (produced.add(name)) {
                    queue += name
                }
            }
        }
        return@lazy produced
    }

    /**
     * Whether withdrawing `required` or removing the property is gated on the Master rules.
     * A payload produced by both sides is: an older starter still has to read what the Master
     * sends, and an older starter still sends the payload the Master reads.
     */
    fun masterProduced(schemaName: String): Boolean =
        server == BOTH || schemaName !in starterProducedSchemas

    /**
     * Who the strict evolution rules name in their failure message.
     */
    fun masterProducerLabel(): String = if (server == BOTH) "both sides" else "the Master"

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

        const val BOTH = "both"

        val SERVER_SIDES = setOf("starter", "master", BOTH)

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
