package contract

import java.io.File
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk

/**
 * The latest release of Axelix the repository knows of, i.e. the greatest vX.Y.Z tag. The
 * stateful contract invariants compare the working tree documents against the contracts as they
 * were at this release: everything the released Master & starters rely on is captured there,
 * while the working tree carries the changes of the release currently being developed.
 *
 * @author Mikhail Polivakha
 */
class ReleaseBaseline private constructor(private val repoRoot: File, val version: Version) {

    val tag: String = "v$version"

    /**
     * The released counterpart of the given working tree contract document, or null when the
     * document did not exist at the release, i.e. the operation is being born right now.
     */
    fun contract(document: File, currentVersion: String): ContractDocument? =
        open(repoRoot).use { repository ->
            val path = document.relativeTo(repoRoot).invariantSeparatorsPath
            val release = RevWalk(repository).use { walk -> walk.parseCommit(repository.resolve(tag)) }

            TreeWalk.forPath(repository, path, release.tree)?.use { blob ->
                ContractDocument.parse(repository.open(blob.getObjectId(0)).bytes, currentVersion)
            }
        }

    companion object {

        private val RELEASE_TAG = Regex("""v\d+\.\d+\.\d+""")

        // TODO:
        //  A null here means the stateful contract checks silently do not run: on a shallow clone,
        //  a fresh fork without tags, or a checkout without .git at all. Whether that should be a
        //  loud skip, a hard failure or something else is deliberately not decided yet.
        /**
         * Finds the latest release the repository knows of, or null when there is no release tag
         * reachable (or no git repository at all).
         */
        fun find(repoRoot: File): ReleaseBaseline? =
            try {
                open(repoRoot).use { gitRepository ->
                    // get git tags
                    gitRepository.refDatabase.getRefsByPrefix("refs/tags/v")
                        // get only non mielstone release tags (our old tags, from the good old days...)
                        .filter { ref -> RELEASE_TAG.matches(ref.name.removePrefix("refs/tags/")) }
                        // parsing the release tag
                        .mapNotNull { ref -> Version.parse(ref.name.removePrefix("refs/tags/v")) }
                        // getting the latest release tag
                        .maxOrNull()
                        ?.let { version -> ReleaseBaseline(repoRoot, version) }
                }
            } catch (noRepository: RepositoryNotFoundException) {
                null
            }

        private fun open(repoRoot: File): Repository =
            FileRepositoryBuilder().setWorkTree(repoRoot).setMustExist(true).build()
    }
}
