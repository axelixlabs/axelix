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
import contract.invariants.stateless.RequiredHonoursTheWindow
import contract.invariants.stateless.ProducedByIsDeclared
import contract.invariants.stateless.StatelessContractInvariant
import java.io.File
import org.gradle.api.GradleException

/**
 * Validation of the Axelix Master <-> starter OpenAPI contract documents.
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
     * Verifies that the contract document complies with the backward compatibility guarantees.
     */
    fun validate(document: File, currentAxelixVersion: String, baseline: ReleaseBaseline? = null) {
        val currentContract = ContractDocument.parse(document, currentAxelixVersion)

        // stateless checks for the current contract
        val problems = STATELESS_INVARIANTS.flatMap { invariant -> invariant.check(currentContract) }.toMutableList()

        if (baseline != null) {
            val releasedContract = baseline.contract(document)
            problems += STATEFUL_INVARIANTS.flatMap { invariant -> invariant.check(releasedContract, currentContract) }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                "The contract document ${document.name} is invalid:\n"
                    + problems.joinToString("\n") { problem -> "  - $problem" })
        }
    }
}
