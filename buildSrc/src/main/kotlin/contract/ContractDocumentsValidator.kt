package contract

import contract.invariants.ContractInvariant
import contract.invariants.DeprecatedIsRemovedAfterTheWindow
import contract.invariants.DeprecationFollowsIntroduction
import contract.invariants.DocumentDescribesOneOperation
import contract.invariants.IntroductionIsDeclared
import contract.invariants.MarkersAreWellFormed
import contract.invariants.RequiredHonoursTheWindow
import contract.invariants.ServerIsDeclared
import contract.invariants.StatefulContractInvariant
import java.io.File
import org.gradle.api.GradleException

/**
 * Validation of the Axelix Master <-> starter OpenAPI contract documents.
 *
 * @author Mikhail Polivakha
 */
object ContractDocumentsValidator {

    private val INVARIANTS: List<ContractInvariant> = listOf(
        DocumentDescribesOneOperation,
        ServerIsDeclared,
        IntroductionIsDeclared,
        MarkersAreWellFormed,
        DeprecationFollowsIntroduction,
        RequiredHonoursTheWindow,
        DeprecatedIsRemovedAfterTheWindow,
    )

    private val STATEFUL_INVARIANTS: List<StatefulContractInvariant> = listOf(
    )

    /**
     * Verifies that the contract document complies with the backward compatibility guarantees.
     */
    fun validate(document: File, currentAxelixVersion: String, baseline: ReleaseBaseline? = null) {
        val currentContract = ContractDocument.parse(document, currentAxelixVersion)

        // stateless checks for the current contract
        val problems = INVARIANTS.flatMap { invariant -> invariant.check(currentContract) }.toMutableList()

        if (baseline != null) {
            val releasedContract = baseline.contract(document, currentAxelixVersion)
            problems += STATEFUL_INVARIANTS.flatMap { invariant -> invariant.check(releasedContract, currentContract) }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                "The contract document ${document.name} is invalid:\n"
                    + problems.joinToString("\n") { problem -> "  - $problem" })
        }
    }
}
