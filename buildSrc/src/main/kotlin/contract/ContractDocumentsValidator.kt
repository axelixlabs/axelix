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

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import contract.invariants.stateless.DeprecatedIsRemovedAfterTheWindow
import contract.invariants.stateless.DeprecationFollowsIntroduction
import contract.invariants.stateless.IntroductionIsDeclared
import contract.invariants.stateless.MarkersAreWellFormed
import contract.invariants.stateless.ProducedByIsDeclared
import contract.invariants.stateless.RequiredHonoursTheWindow
import contract.invariants.stateless.StatelessContractInvariant
import java.io.File
import org.gradle.api.GradleException

/**
 * Validation of the schema bundles in the Axelix Master <-> starter OpenAPI documents.
 *
 * @author Mikhail Polivakha
 */
object ContractDocumentsValidator {

    private val STATELESS_INVARIANTS: List<StatelessContractInvariant> = listOf(
        ProducedByIsDeclared,
        IntroductionIsDeclared,
        MarkersAreWellFormed,
        DeprecationFollowsIntroduction,
        RequiredHonoursTheWindow,
        DeprecatedIsRemovedAfterTheWindow,
    )

    /** Verifies that every schema in the document complies with the compatibility guarantees. */
    fun validate(document: File, currentAxelixVersion: String) {
        val root = YAMLMapper().readTree(document)
        val currentSchemas = ContractSchema.parse(root, currentAxelixVersion)
        val problems = mutableListOf<String>()

        val bundleVersion = root.path("info").path("version")
        if (!bundleVersion.isTextual || bundleVersion.textValue().isNotEmpty()) {
            problems += "the 'info.version' must be an empty string because versions apply to schemas, not schema bundles"
        }

        problems += currentSchemas.flatMap { schema ->
            STATELESS_INVARIANTS.flatMap { invariant -> invariant.check(schema) }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                "The contract document ${document.name} is invalid:\n"
                    + problems.joinToString("\n") { problem -> "  - $problem" })
        }
    }
}
