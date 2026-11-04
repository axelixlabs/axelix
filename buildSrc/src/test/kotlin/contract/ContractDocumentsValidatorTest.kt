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

import contract.invariants.stateful.ReleaseBaseline
import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.PersonIdent
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Unit tests for {@link ContractDocumentsValidator}.
 *
 * @author Mikhail Polivakha
 */
class ContractDocumentsValidatorTest {

    companion object {
        private const val CURRENT_VERSION = "1.2.0-SNAPSHOT"

        private val COMMITTER = PersonIdent("test", "test@axelix.io")

        private val RELEASED_BUNDLE = """
            openapi: 3.1.0
            info:
              title: bundle
              version: ""
            components:
              schemas:
                Gone:
                  x-axelix-produced-by: master
                  x-axelix-introduced-in: 1.0.0
                  type: object
                  properties:
                    kept:
                      type: string
                      x-axelix-introduced-in: 1.0.0
                Shared:
                  x-axelix-produced-by: master
                  x-axelix-introduced-in: 1.0.0
                  type: object
                  properties:
                    value:
                      type: string
                      x-axelix-introduced-in: 1.0.0
            """.trimIndent()

        private val CURRENT_BUNDLE = """
            openapi: 3.1.0
            info:
              title: bundle
              version: ""
            components:
              schemas:
                Shared:
                  x-axelix-produced-by: master
                  x-axelix-introduced-in: 1.0.0
                  type: object
                  properties:
                    value:
                      type: integer
                      x-axelix-introduced-in: 1.0.0
                Born:
                  x-axelix-produced-by: master
                  x-axelix-introduced-in: 1.0.0
                  type: object
                  properties:
                    fresh:
                      type: string
                      x-axelix-introduced-in: 1.2.0
            """.trimIndent()
    }

    @TempDir
    lateinit var repoRoot: File

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
    fun `a non-empty info version fails`() {
        assertThrows(GradleException::class.java) {
            ContractDocumentsValidator.validate(document("non-empty-info-version.yaml"), CURRENT_VERSION)
        }
    }

    @Test
    fun `a missing info version fails`() {
        assertThrows(GradleException::class.java) {
            ContractDocumentsValidator.validate(document("missing-info-version.yaml"), CURRENT_VERSION)
        }
    }

    @Test
    fun `a missing produced-by marker fails`() {
        expectProblem(
            "stateless/ProducedByIsDeclared/missing-produced-by.yaml",
            "the schema 'LogLevelChangeRequest' must declare 'x-axelix-produced-by' as one of [starter, master]")
    }

    @Test
    fun `an unknown producer fails`() {
        expectProblem(
            "stateless/ProducedByIsDeclared/unknown-produced-by.yaml",
            "the schema 'LogLevelChangeRequest' must declare 'x-axelix-produced-by' as one of [starter, master]")
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
            "the schema 'LogLevelChangeRequest' has 'x-axelix-introduced-in: 1.0' that is not of the x.y.z form")
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
    fun `stateful checks pair schemas by name even when their order differs`() {
        Git.init().setDirectory(repoRoot).call().use { git ->
            writeBundle(RELEASED_BUNDLE)
            commit(git, "released")
            git.tag().setName("v1.1.0").setTagger(COMMITTER).call()
            writeBundle(CURRENT_BUNDLE)
        }

        val failure = assertThrows(GradleException::class.java) {
            ContractDocumentsValidator.validate(
                repoRoot.resolve("bundle.yaml"), CURRENT_VERSION, ReleaseBaseline.find(repoRoot))
        }

        // Shared stays paired with Shared after the reorder, and Born is judged as a schema born now.
        assertEquals(
            "The contract document bundle.yaml is invalid:\n"
                + "  - the property 'Shared.value' changed its 'type' from 'string' to 'integer' "
                + "against the released contract (1.1.0): a released property must never "
                + "be redefined in place, introduce a new property and deprecate the old one instead\n"
                + "  - the schema 'Born' is born in this release but has 'x-axelix-introduced-in: 1.0.0' "
                + "instead of the version currently being built (1.2.0)",
            failure.message)
    }

    @Test
    fun `a deprecated property that outlived the compatibility window fails`() {
        expectProblem(
            "stateless/DeprecatedIsRemovedAfterTheWindow/overdue-deprecation.yaml",
            "the property 'LogLevelChangeRequest.ttlSeconds' was deprecated in 1.1.0 and the "
                + "compatibility window has passed: remove it from the contract",
            currentVersion = "1.4.0-SNAPSHOT")
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

    private fun writeBundle(content: String) {
        repoRoot.resolve("bundle.yaml").writeText(content)
    }

    private fun commit(git: Git, message: String) {
        git.add().addFilepattern(".").call()
        git.commit()
            .setMessage(message).setAuthor(COMMITTER).setCommitter(COMMITTER)
            .setAllowEmpty(true).setSign(false)
            .call()
    }
}
