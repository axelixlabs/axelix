package contract

import contract.invariants.ContractInvariant
import contract.invariants.DeprecatedIsRemovedAfterTheWindow
import contract.invariants.DeprecationFollowsIntroduction
import contract.invariants.DocumentDescribesOneOperation
import contract.invariants.IntroductionIsDeclared
import contract.invariants.MarkersAreWellFormed
import contract.invariants.RequiredHonoursTheWindow
import contract.invariants.ServerIsDeclared
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

    /**
     * Verifies that the contract document complies with the backward compatibility guarantees.
     */
    fun validate(document: File, currentVersion: String) {
        val parsed = ContractDocument.parse(document, currentVersion)

        val problems = INVARIANTS.flatMap { invariant -> invariant.check(parsed) }
        if (problems.isNotEmpty()) {
            throw GradleException(
                "The contract document ${document.name} is invalid:\n"
                    + problems.joinToString("\n") { problem -> "  - $problem" })
        }
    }
}
