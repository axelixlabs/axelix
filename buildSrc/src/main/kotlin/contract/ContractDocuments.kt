package contract

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.io.File
import org.gradle.api.GradleException

/**
 * Validation of the Axelix Master <-> starter OpenAPI contract documents
 *
 * @author Mikhail Polivakha
 */
object ContractDocuments {

    private const val INTRODUCED_IN = "x-axelix-introduced-in"

    /**
     * Fails the build unless the document declares the Axelix version every part of the contract
     * was introduced in: once on the 'info' block for the operation itself, and once on EVERY
     * property of every schema. The per-property version is what answers "may the Master already
     * rely on this property / drop the support of it", so a property without it is a bug waiting
     * to happen — there is deliberately no fallback to a document-level default.
     */
    fun validate(document: File) {
        val root = YAMLMapper().readTree(document)
        val problems = mutableListOf<String>()

        if (root.path("info").path(INTRODUCED_IN).isMissingNode) {
            problems += "the 'info' block is missing '$INTRODUCED_IN'"
        }

        root.path("components").path("schemas").properties().forEach { (schemaName, schema) ->
            schema.path("properties").properties().forEach { (propertyName, property) ->
                if (property.path(INTRODUCED_IN).isMissingNode) {
                    problems += "the property '$schemaName.$propertyName' is missing '$INTRODUCED_IN'"
                }
            }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                "The contract document ${document.name} is invalid:\n"
                    + problems.joinToString("\n") { problem -> "  - $problem" })
        }
    }
}
