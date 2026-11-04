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
 * A parsed contract between Axelix Master and the starters. The document defines payload schemas.
 * Each schema declares who produces it and when it was introduced.
 *
 * @param axelixVersion version of Axelix, when the given document was actaul
 * @param root the [JsonNode] that represents the root of the YAML document to be inspected
 *
 * @author Mikhail Polivakha
 */
class ContractDocument private constructor(private val root: JsonNode, val axelixVersion: Version) {

    val info: JsonNode = root.path("info")

    /**
     * Every part of the document that carries lifecycle markers.
     */
    val markedParts: List<MarkedPart>

    val schemas: List<Schema>

    val properties: List<Property>

    init {
        schemas = root.path("components").path("schemas").properties().map { (schemaName, schema) ->
            Schema(schemaName, schema)
        }
        properties = schemas.flatMap { schema ->

            // The inline 'allOf' parts (e.g. subtypes) declare their own properties.
            (listOf(schema.node) + schema.node.path("allOf").toList()).flatMap { part ->
                val requiredProperties = part.path("required").map { name -> name.asText() }.toSet()

                part.path("properties").properties().map { (propertyName, propertyNode) ->
                    Property(schema.name, propertyName, propertyNode, propertyName in requiredProperties)
                }
            }
        }
        markedParts = listOf(MarkedPart("the 'info' block", info)) +
            schemas.map { schema -> MarkedPart(schema.location, schema.node) } +
            properties.map { property -> MarkedPart(property.location, property.node) }
    }

    /**
     * The names of the schemas produced by the starter, taken from [PRODUCED_BY] on each schema.
     */
    val starterProducedSchemas: Set<String> by lazy {
        schemas.filter { schema -> schema.producedBy == "starter" }.map { schema -> schema.name }.toSet()
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

    class Schema(val name: String, val node: JsonNode) {

        val location: String = "the schema '$name'"

        /** The side that produces this schema, or empty when the marker is absent. */
        val producedBy: String = node.path(PRODUCED_BY).asText("")
    }

    data class Property(
        val schemaName: String, val name: String, val node: JsonNode, val required: Boolean) {

        val location: String = "the property '$schemaName.$name'"
    }

    companion object {

        const val INTRODUCED_IN = "x-axelix-introduced-in"
        const val DEPRECATED_IN = "x-axelix-deprecated-in"
        const val PRODUCED_BY = "x-axelix-produced-by"

        val PRODUCERS = setOf("starter", "master")

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
