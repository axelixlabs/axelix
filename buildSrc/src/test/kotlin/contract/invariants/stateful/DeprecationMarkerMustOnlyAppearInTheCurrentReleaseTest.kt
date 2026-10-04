package contract.invariants.stateful

import contract.ContractDocument
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for {@link DeprecationMarkerMustOnlyAppearInTheCurrentRelease}.
 *
 * @author Mikhail Polivakha
 */
class DeprecationMarkerMustOnlyAppearInTheCurrentReleaseTest {

    companion object {
        private const val CURRENT_VERSION = "1.2.0-SNAPSHOT"
        private const val RELEASED_VERSION = "1.1.0"
    }

    @Test
    fun `a released deprecation retained as-is passes`() {
        val problems = DeprecationMarkerMustOnlyAppearInTheCurrentRelease
            .check(baseline(), current("deprecation-retained.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a deprecation appearing with the version being built passes`() {
        val problems = DeprecationMarkerMustOnlyAppearInTheCurrentRelease
            .check(baseline(), current("deprecation-appears-now.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a born document is not judged`() {
        val problems = DeprecationMarkerMustOnlyAppearInTheCurrentRelease
            .check(null, current("born-document.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a deprecation appearing with an already released version fails`() {
        val problems = DeprecationMarkerMustOnlyAppearInTheCurrentRelease
            .check(baseline(), current("deprecation-backdated.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.configuredLevel' has 'x-axelix-deprecated-in: 1.1.0' "
                    + "that the released contract (1.1.0) does not: a deprecation that is supposed to appear "
                    + "in the upcoming release must specify the version currently being built (1.2.0)"),
            problems)
    }

    @Test
    fun `a rewritten released deprecation fails`() {
        val problems = DeprecationMarkerMustOnlyAppearInTheCurrentRelease
            .check(baseline(), current("deprecation-rewritten.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.ttlSeconds' has 'x-axelix-deprecated-in: 1.2.0' "
                    + "but the released contract (1.1.0) already deprecates it in 1.1.0: "
                    + "the deprecation marker must never change"),
            problems)
    }

    private fun baseline(): ContractDocument =
        ContractDocument.parse(document("released-baseline.yaml"), RELEASED_VERSION)

    private fun current(name: String): ContractDocument =
        ContractDocument.parse(document(name), CURRENT_VERSION)

    private fun document(name: String): File =
        File(javaClass.getResource(
            "/contract/stateful/${javaClass.simpleName.removeSuffix("Test")}/$name")!!.toURI())
}
