package contract.invariants.stateful

import contract.ContractDocument
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for {@link PropertyRemovalMustFollowDeprecation}.
 *
 * @author Mikhail Polivakha
 */
class PropertyRemovalMustFollowDeprecationTest {

    companion object {
        private const val WITHIN_WINDOW_VERSION = "1.2.0-SNAPSHOT"
        private const val AFTER_WINDOW_VERSION = "1.4.0-SNAPSHOT"
        private const val RELEASED_VERSION = "1.1.0"
    }

    @Test
    fun `an unchanged document passes`() {
        val problems = PropertyRemovalMustFollowDeprecation
            .check(baseline(), current("unchanged.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a born document is not judged`() {
        val problems = PropertyRemovalMustFollowDeprecation
            .check(null, current("born-document.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a master-produced property removed without prior deprecation fails`() {
        val problems = PropertyRemovalMustFollowDeprecation
            .check(baseline(), current("master-produced-removed-without-deprecation.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.undeprecatedMaster' is produced by the Master and "
                    + "was removed without prior deprecation against the released contract (1.1.0): "
                    + "deprecate the property and keep producing it until the window passes before "
                    + "removing it"),
            problems)
    }

    @Test
    fun `a master-produced property removed within its deprecation window fails`() {
        val problems = PropertyRemovalMustFollowDeprecation
            .check(baseline(), current("master-produced-removed-within-window.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.deprecatedMaster' was removed while its deprecation "
                    + "window (deprecated in 1.1.0) has not passed yet: keep it in the contract until the "
                    + "window closes"),
            problems)
    }

    @Test
    fun `a master-produced property removed after its deprecation window passes`() {
        val problems = PropertyRemovalMustFollowDeprecation
            .check(baseline(), current("master-produced-removed-after-window.yaml", AFTER_WINDOW_VERSION))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a property produced by starter and master removed without prior deprecation fails`() {
        val problems = PropertyRemovalMustFollowDeprecation
            .check(
                baseline("released-baseline-starter-and-master.yaml"),
                current("removed-without-deprecation-starter-and-master.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.undeprecatedShared' is produced by the Master and "
                    + "was removed without prior deprecation against the released contract (1.1.0): "
                    + "deprecate the property and keep producing it until the window passes before "
                    + "removing it"),
            problems)
    }

    @Test
    fun `a pure removal of a starter-produced property passes`() {
        val problems = PropertyRemovalMustFollowDeprecation
            .check(baseline(), current("starter-produced-pure-removal.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a starter-produced property removed within its deprecation window fails`() {
        val problems = PropertyRemovalMustFollowDeprecation
            .check(baseline(), current("starter-produced-removed-within-window.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeReply.deprecatedStarter' was removed while its deprecation "
                    + "window (deprecated in 1.1.0) has not passed yet: keep it in the contract until the "
                    + "window closes"),
            problems)
    }

    @Test
    fun `a starter-produced property removed after its deprecation window passes`() {
        val problems = PropertyRemovalMustFollowDeprecation
            .check(baseline(), current("starter-produced-removed-after-window.yaml", AFTER_WINDOW_VERSION))

        assertEquals(emptyList<String>(), problems)
    }

    private fun baseline(name: String = "released-baseline.yaml"): ContractDocument =
        ContractDocument.parse(document(name), RELEASED_VERSION)

    private fun current(name: String, version: String = WITHIN_WINDOW_VERSION): ContractDocument =
        ContractDocument.parse(document(name), version)

    private fun document(name: String): File =
        File(javaClass.getResource(
            "/contract/stateful/${javaClass.simpleName.removeSuffix("Test")}/$name")!!.toURI())
}
