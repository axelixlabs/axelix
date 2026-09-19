package contract

import contract.invariants.stateful.IntroductionMarkerMustNeverChange
import contract.invariants.stateful.NewPartsMustBeMarkedAsIntroducedInTheCurrentRelease
import contract.invariants.stateful.ReleaseBaseline
import contract.invariants.stateful.StatefulContractInvariant
import contract.invariants.stateless.DeprecatedIsRemovedAfterTheWindow
import contract.invariants.stateless.DeprecationFollowsIntroduction
import contract.invariants.stateless.DocumentDescribesOneOperation
import contract.invariants.stateless.IntroductionIsDeclared
import contract.invariants.stateless.MarkersAreWellFormed
import contract.invariants.stateless.RequiredHonoursTheWindow
import contract.invariants.stateless.ServerIsDeclared
import contract.invariants.stateless.StatelessContractInvariant
import java.io.File
import org.gradle.api.GradleException

/**
 * Validation of the Axelix Master <-> starter OpenAPI contract documents.
 *
 * @author Mikhail Polivakha
 */
object ContractDocumentsValidator {

    private val INVARIANTS: List<StatelessContractInvariant> = listOf(
        DocumentDescribesOneOperation,
        ServerIsDeclared,
        IntroductionIsDeclared,
        MarkersAreWellFormed,
        DeprecationFollowsIntroduction,
        RequiredHonoursTheWindow,
        DeprecatedIsRemovedAfterTheWindow,
    )

    private val STATEFUL_INVARIANTS: List<StatefulContractInvariant> = listOf(
        NewPartsMustBeMarkedAsIntroducedInTheCurrentRelease,
        IntroductionMarkerMustNeverChange,
    )

    /**
     * Verifies that the contract document complies with the backward compatibility guarantees.
     */
    fun validate(document: File, currentAxelixVersion: String, baseline: ReleaseBaseline? = null) {
        val currentContract = ContractDocument.parse(document, currentAxelixVersion)

        // stateless checks for the current contract
        val problems = INVARIANTS.flatMap { invariant -> invariant.check(currentContract) }.toMutableList()

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
