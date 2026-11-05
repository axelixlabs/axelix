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
import contract.invariants.stateful.DeprecationMarkerMustOnlyAppearInTheCurrentRelease
import contract.invariants.stateful.IntroductionMarkerMustNeverChange
import contract.invariants.stateful.MasterProducedPropertyMustNotBecomeOptional
import contract.invariants.stateful.NewPartsMustBeMarkedAsIntroducedInTheCurrentRelease
import contract.invariants.stateful.PropertyRemovalMustFollowDeprecation
import contract.invariants.stateful.PropertyTypeAndFormatMustNeverChange
import contract.invariants.stateful.ReleaseBaseline
import contract.invariants.stateful.StatefulContractInvariant
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

    private val STATEFUL_INVARIANTS: List<StatefulContractInvariant> = listOf(
        NewPartsMustBeMarkedAsIntroducedInTheCurrentRelease,
        IntroductionMarkerMustNeverChange,
        DeprecationMarkerMustOnlyAppearInTheCurrentRelease,
        PropertyTypeAndFormatMustNeverChange,
        MasterProducedPropertyMustNotBecomeOptional,
        PropertyRemovalMustFollowDeprecation,
    )

    /**
     * Verifies that every schema in the document complies with the compatibility guarantees.
     *
     * When [baseline] is present, each schema is also compared with the released schema of the
     * same name. The name is the schema's identity: a name only in the working tree is a schema
     * born in this release, and a name only in the release is a schema that was removed.
     */
    fun validate(document: File, currentAxelixVersion: String, baseline: ReleaseBaseline? = null) {
        val root = YAMLMapper().readTree(document)
        val currentSchemas = ContractSchema.parse(root, currentAxelixVersion)

        // stateless checks for the current contract
        val problems = currentSchemas.flatMap { schema ->
            STATELESS_INVARIANTS.flatMap { invariant -> invariant.check(schema) }
        }.toMutableList()

        if (baseline != null) {
            val releasedByName = baseline.contract(document).orEmpty().associateBy { schema -> schema.name }
            val currentByName = currentSchemas.associateBy { schema -> schema.name }
            // Working-tree order first, then schemas the release still has and the working tree dropped.
            val names = (currentSchemas.map { schema -> schema.name } + releasedByName.keys).distinct()

            problems += names.flatMap { name ->
                STATEFUL_INVARIANTS.flatMap { invariant ->
                    invariant.check(releasedByName[name], currentByName[name])
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
