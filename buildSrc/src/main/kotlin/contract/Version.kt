package contract

// TODO:
//  We have the SemanticVersion already in the common. I am not sure whether or not we should re-use it somehow.
//  That would require a lot of machinery that I think right now is unnecessary.
/**
 * An Axelix release version. The qualifier (e.g. -SNAPSHOT) is irrelevant to every compatibility
 * rule and is dropped at parsing.
 *
 * @author Mikhail Polivakha
 */
data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {

    override fun compareTo(other: Version): Int =
        compareValuesBy(this, other, Version::major, Version::minor, Version::patch)

    override fun toString(): String = "$major.$minor.$patch"

    companion object {

        private val FORMAT = Regex("""(\d+)\.(\d+)\.(\d+)""")

        fun parse(value: String): Version? {
            val match = FORMAT.matchAt(value, 0) ?: return null
            val (major, minor, patch) = match.destructured
            return Version(major.toInt(), minor.toInt(), patch.toInt())
        }
    }
}
