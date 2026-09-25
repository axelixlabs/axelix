package contract

import java.io.File
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

/**
 * Unit tests for {@link ContractDocumentsValidator}.
 *
 * @author Mikhail Polivakha
 */
class ContractDocumentsValidatorTest {

    companion object {
        private const val CURRENT_VERSION = "1.2.0-SNAPSHOT"
    }

    @Test
    fun `a compliant document passes`() {
        ContractDocumentsValidator.validate(document("compliant.yaml"), CURRENT_VERSION)
    }

    @Test
    fun `a deprecation strictly later than the introduction passes`() {
        ContractDocumentsValidator.validate(
            document("stateless/DeprecationFollowsIntroduction/deprecation-strictly-later-than-introduction.yaml"), CURRENT_VERSION)
    }

    @Test
    fun `a marker equal to the version being built passes`() {
        ContractDocumentsValidator.validate(
            document("stateless/MarkersAreWellFormed/marker-equal-to-current-version.yaml"), CURRENT_VERSION)
    }

    @Test
    fun `a missing server marker fails`() {
        expectProblem(
            "stateless/ServerIsDeclared/missing-server.yaml",
            "the 'info' block must declare 'x-axelix-server' as one of [starter, master]")
    }

    @Test
    fun `an unknown server side fails`() {
        expectProblem(
            "stateless/ServerIsDeclared/unknown-server.yaml",
            "the 'info' block must declare 'x-axelix-server' as one of [starter, master]")
    }

    @Test
    fun `a property without an introduction marker fails`() {
        expectProblem(
            "stateless/IntroductionIsDeclared/property-missing-introduction.yaml",
            "the property 'LogLevelChangeRequest.configuredLevel' is missing 'x-axelix-introduced-in'")
    }

    @Test
    fun `a property inside an allOf part without an introduction marker fails`() {
        expectProblem(
            "stateless/IntroductionIsDeclared/allof-property-missing-introduction.yaml",
            "the property 'Dog.barkVolume' is missing 'x-axelix-introduced-in'")
    }

    @Test
    fun `a malformed version marker fails`() {
        expectProblem(
            "stateless/MarkersAreWellFormed/malformed-marker.yaml",
            "the 'info' block has 'x-axelix-introduced-in: 1.0' that is not of the x.y.z form")
    }

    @Test
    fun `a marker ahead of the version being built fails`() {
        expectProblem(
            "stateless/MarkersAreWellFormed/marker-ahead-of-current-version.yaml",
            "the property 'LogLevelChangeRequest.configuredLevel' has 'x-axelix-introduced-in: 1.3.0' "
                + "that is ahead of the version currently being built (1.2.0)")
    }

    @Test
    fun `a deprecation in the release that introduced the property fails`() {
        expectProblem(
            "stateless/DeprecationFollowsIntroduction/deprecation-in-introduction-release.yaml",
            "the property 'LogLevelChangeRequest.configuredLevel' has 'x-axelix-deprecated-in: 1.0.0' "
                + "that is not strictly later than 'x-axelix-introduced-in: 1.0.0'")
    }

    @Test
    fun `a late required property of a starter-produced payload fails within the window`() {
        expectProblem(
            "stateless/RequiredHonoursTheWindow/premature-required.yaml",
            "the property 'LoggersReply.effectiveLevel' cannot be 'required' yet: starters older "
                + "than 1.2.0 do not send it and only leave the compatibility window in 1.5")
    }

    @Test
    fun `a late required property of a discriminator subtype of a starter-produced payload fails within the window`() {
        expectProblem(
            "stateless/RequiredHonoursTheWindow/premature-required-in-discriminator-subtype.yaml",
            "the property 'Dog.barkVolume' cannot be 'required' yet: starters older "
                + "than 1.2.0 do not send it and only leave the compatibility window in 1.5")
    }

    @Test
    fun `a late required property of a starter-produced payload passes once the window elapsed`() {
        ContractDocumentsValidator.validate(document("stateless/RequiredHonoursTheWindow/required-after-window.yaml"), "1.5.0-SNAPSHOT")
    }

    @Test
    fun `a late required property of a master-produced payload passes right away`() {
        ContractDocumentsValidator.validate(document("stateless/RequiredHonoursTheWindow/master-produced-late-required.yaml"), CURRENT_VERSION)
    }

    @Test
    fun `a deprecated property that outlived the compatibility window fails`() {
        expectProblem(
            "stateless/DeprecatedIsRemovedAfterTheWindow/overdue-deprecation.yaml",
            "the property 'LogLevelChangeRequest.ttlSeconds' was deprecated in 1.1.0 and the "
                + "compatibility window has passed: remove it from the contract",
            currentVersion = "1.4.0-SNAPSHOT")
    }

    @Test
    fun `a document with two operations fails`() {
        expectProblem(
            "stateless/DocumentDescribesOneOperation/two-operations.yaml",
            "the document must describe exactly one operation, but describes 2: "
                + "GET /actuator/axelix-loggers, POST /actuator/axelix-loggers/logger/{name}/reset")
    }

    @Test
    fun `a document without operations fails`() {
        expectProblem(
            "stateless/DocumentDescribesOneOperation/no-operations.yaml",
            "the document must describe exactly one operation, but describes none")
    }

    private fun expectProblem(
        documentName: String, expectedProblem: String, currentVersion: String = CURRENT_VERSION) {
        try {
            ContractDocumentsValidator.validate(document(documentName), currentVersion)
            fail<Unit>("The validation was expected to fail with: $expectedProblem")
        } catch (expected: GradleException) {
            assertTrue(expected.message!!.contains(expectedProblem)) {
                "The failure does not mention '$expectedProblem':\n${expected.message}"
            }
        }
    }

    private fun document(name: String): File =
        File(javaClass.getResource("/contract/$name")!!.toURI())
}
