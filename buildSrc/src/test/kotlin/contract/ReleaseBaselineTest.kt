package contract

import contract.invariants.stateful.ReleaseBaseline
import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.PersonIdent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Unit tests for {@link ReleaseBaseline}.
 *
 * @author Mikhail Polivakha
 */
class ReleaseBaselineTest {

    companion object {
        private const val CURRENT_VERSION = "1.2.0-SNAPSHOT"
        private const val CONTRACT_PATH = "common/src/main/resources/contract/logger/logger-level-change.yaml"

        private val COMMITTER = PersonIdent("test", "test@axelix.io")
    }

    @TempDir
    lateinit var repoRoot: File

    @Test
    fun `a directory that is not a git repository yields no baseline`() {
        assertNull(ReleaseBaseline.find(repoRoot))
    }

    @Test
    fun `a repository without release tags yields no baseline`() {
        Git.init().setDirectory(repoRoot).call().use { git ->
            commit(git, "first")
        }
        assertNull(ReleaseBaseline.find(repoRoot))
    }

    @Test
    fun `the greatest release tag wins by semver and milestones do not count`() {
        Git.init().setDirectory(repoRoot).call().use { git ->
            commit(git, "first")
            git.tag().setName("v1.9.0").setTagger(COMMITTER).call()
            git.tag().setName("v1.10.0").setTagger(COMMITTER).call()
            git.tag().setName("v1.11.0-M1").setTagger(COMMITTER).call()
        }

        assertEquals(Version(1, 10, 0), ReleaseBaseline.find(repoRoot)!!.version)
    }

    @Test
    fun `the released contract is read from the tag and not from the working tree`() {
        Git.init().setDirectory(repoRoot).call().use { git ->
            contract(server = "starter")
            commit(git, "released")
            git.tag().setName("v1.1.0").setTagger(COMMITTER).call()

            contract(server = "master")
            commit(git, "developed")
        }

        val released = ReleaseBaseline
            .find(repoRoot)!!
            .contract(repoRoot.resolve(CONTRACT_PATH))!!
        assertEquals("starter", released.server)
        assertEquals(Version.parse("v1.1.0"), released.axelixVersion)
    }

    @Test
    fun `a document absent at the release is recognized as born now`() {
        Git.init().setDirectory(repoRoot).call().use { git ->
            commit(git, "released")
            git.tag().setName("v1.1.0").setTagger(COMMITTER).call()

            contract(server = "starter")
        }

        val baseline = ReleaseBaseline.find(repoRoot)!!
        assertNull(baseline.contract(repoRoot.resolve(CONTRACT_PATH)))
    }

    private fun contract(server: String) {
        val document = repoRoot.resolve(CONTRACT_PATH)
        document.parentFile.mkdirs()
        document.writeText(
            """
            openapi: 3.0.3
            info:
              title: Log level change
              version: 1.0.0
              x-axelix-server: $server
              x-axelix-introduced-in: 1.0.0
            paths:
              /actuator/axelix-loggers/{name}:
                post:
                  responses:
                    '200':
                      description: Changed
            """.trimIndent())
    }

    private fun commit(git: Git, message: String) {
        git.add().addFilepattern(".").call()
        git.commit()
            .setMessage(message).setAuthor(COMMITTER).setCommitter(COMMITTER)
            .setAllowEmpty(true).setSign(false)
            .call()
    }
}
