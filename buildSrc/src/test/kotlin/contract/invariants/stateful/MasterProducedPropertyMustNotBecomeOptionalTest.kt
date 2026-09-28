package contract.invariants.stateful

import contract.ContractDocument
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for {@link MasterProducedPropertyMustNotBecomeOptional}.
 *
 * @author Mikhail Polivakha
 */
class MasterProducedPropertyMustNotBecomeOptionalTest {

    companion object {
        private const val CURRENT_VERSION = "1.2.0-SNAPSHOT"
        private const val RELEASED_VERSION = "1.1.0"
    }

    @Test
    fun `unchanged required properties pass`() {
        val problems = MasterProducedPropertyMustNotBecomeOptional
            .check(baseline(), current("unchanged.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a born document is not judged`() {
        val problems = MasterProducedPropertyMustNotBecomeOptional
            .check(null, current("born-document.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a withdrawn required of a starter-produced property passes`() {
        val problems = MasterProducedPropertyMustNotBecomeOptional
            .check(baseline(), current("starter-produced-required-withdrawn.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a withdrawn required of a master-produced property fails`() {
        val problems = MasterProducedPropertyMustNotBecomeOptional
            .check(baseline(), current("required-withdrawn.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.configuredLevel' is produced by the Master and is "
                    + "'required' in the released contract (1.1.0): withdrawing 'required' is a hidden "
                    + "removal, deprecate the property instead and keep it required until the window passes"),
            problems)
    }

    @Test
    fun `a withdrawn required of a property produced by starter and master sides fails`() {
        val problems = MasterProducedPropertyMustNotBecomeOptional
            .check(baseline("released-baseline-starter-and-master.yaml"), current("required-withdrawn-starter-and-master.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.configuredLevel' is produced by the Master and is "
                    + "'required' in the released contract (1.1.0): withdrawing 'required' is a hidden "
                    + "removal, deprecate the property instead and keep it required until the window passes"),
            problems)
    }

    private fun baseline(name: String = "released-baseline.yaml"): ContractDocument =
        ContractDocument.parse(document(name), RELEASED_VERSION)

    private fun current(name: String): ContractDocument =
        ContractDocument.parse(document(name), CURRENT_VERSION)

    private fun document(name: String): File =
        File(javaClass.getResource(
            "/contract/stateful/${javaClass.simpleName.removeSuffix("Test")}/$name")!!.toURI())
}
