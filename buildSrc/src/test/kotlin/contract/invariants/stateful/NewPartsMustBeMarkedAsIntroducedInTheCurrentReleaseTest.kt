package contract.invariants.stateful

import contract.ContractDocument
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for {@link NewPartsAreIntroducedInTheCurrentRelease}.
 *
 * @author Mikhail Polivakha
 */
class NewPartsMustBeMarkedAsIntroducedInTheCurrentReleaseTest {

    companion object {
        private const val CURRENT_VERSION = "1.2.0-SNAPSHOT"
        private const val RELEASED_VERSION = "1.1.0"
    }

    @Test
    fun `a new property stamped with the version being built passes`() {
        val problems = NewPartsMustBeMarkedAsIntroducedInTheCurrentRelease
            .check(baseline(), current("new-property-introduced-now.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a new property backdated to an already released version fails`() {
        val problems = NewPartsMustBeMarkedAsIntroducedInTheCurrentRelease
            .check(baseline(), current("new-property-backdated.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.ttlSeconds' is born in this release but has "
                    + "'x-axelix-introduced-in: 1.1.0' instead of the version currently being built (1.2.0)"),
            problems)
    }

    @Test
    fun `a born document stamped with the version being built passes`() {
        val problems = NewPartsMustBeMarkedAsIntroducedInTheCurrentRelease
            .check(null, current("born-document-introduced-now.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a born document backdated fails on every backdated part`() {
        val problems = NewPartsMustBeMarkedAsIntroducedInTheCurrentRelease
            .check(null, current("born-document-backdated.yaml"))

        assertEquals(
            listOf(
                "the 'info' block is born in this release but has "
                    + "'x-axelix-introduced-in: 1.1.0' instead of the version currently being built (1.2.0)",
                "the property 'LogLevelChangeRequest.configuredLevel' is born in this release but has "
                    + "'x-axelix-introduced-in: 1.0.0' instead of the version currently being built (1.2.0)"),
            problems)
    }

    private fun baseline(): ContractDocument =
        ContractDocument.parse(document("released-baseline.yaml"), RELEASED_VERSION)

    private fun current(name: String): ContractDocument =
        ContractDocument.parse(document(name), CURRENT_VERSION)

    private fun document(name: String): File =
        File(javaClass.getResource("/contract/stateful/$name")!!.toURI())
}
