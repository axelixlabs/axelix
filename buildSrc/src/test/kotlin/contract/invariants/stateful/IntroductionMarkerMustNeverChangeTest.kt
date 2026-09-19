package contract.invariants.stateful

import contract.ContractDocument
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for {@link IntroductionMarkerMustNeverChange}.
 *
 * @author Mikhail Polivakha
 */
class IntroductionMarkerMustNeverChangeTest {

    companion object {
        private const val CURRENT_VERSION = "1.2.0-SNAPSHOT"
        private const val RELEASED_VERSION = "1.1.0"
    }

    @Test
    fun `unchanged introduction markers pass, a new property is not judged`() {
        val problems = IntroductionMarkerMustNeverChange
            .check(baseline(), current("new-property-introduced-now.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a born document is not judged`() {
        val problems = IntroductionMarkerMustNeverChange
            .check(null, current("born-document-introduced-now.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a rewritten introduction marker of a released property fails`() {
        val problems = IntroductionMarkerMustNeverChange
            .check(baseline(), current("introduction-rewritten-on-property.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.configuredLevel' has 'x-axelix-introduced-in: 1.2.0' "
                    + "but the released contract (1.1.0) knows it as introduced in 1.0.0: "
                    + "the introduction marker may never change"),
            problems)
    }

    @Test
    fun `a rewritten introduction marker of a released operation fails`() {
        val problems = IntroductionMarkerMustNeverChange
            .check(baseline(), current("introduction-rewritten-on-info.yaml"))

        assertEquals(
            listOf(
                "the 'info' block has 'x-axelix-introduced-in: 1.1.0' "
                    + "but the released contract (1.1.0) knows it as introduced in 1.0.0: "
                    + "the introduction marker may never change"),
            problems)
    }

    private fun baseline(): ContractDocument =
        ContractDocument.parse(document("released-baseline.yaml"), RELEASED_VERSION)

    private fun current(name: String): ContractDocument =
        ContractDocument.parse(document(name), CURRENT_VERSION)

    private fun document(name: String): File =
        File(javaClass.getResource("/contract/stateful/$name")!!.toURI())
}
