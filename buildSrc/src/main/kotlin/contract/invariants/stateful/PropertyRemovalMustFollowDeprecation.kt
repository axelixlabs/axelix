package contract.invariants.stateful

import contract.ContractDocument
import contract.ContractDocument.Companion.DEPRECATED_IN

/**
 * A property the latest release already carried must not simply vanish: dropping it is a removal,
 * and the regulation lets a removal happen only once no released peer within the compatibility
 * window still depends on the property.
 *
 * Master-produced properties (M->S requests and responses) are the strict case: every released
 * starter parses them, so the Master must keep producing the property until it was deprecated and
 * its window has passed. A removal that was never preceded by a deprecation, or one that jumps the
 * window, silently breaks starters that still expect the field.
 *
 * Starter-produced properties (S->M) are the lenient case: the Master is never older than the
 * starter it talks to, so a *pure* removal is immediate — the field can be dropped from the
 * contract at once.
 *
 * The one exception is when the removal itself is just one half of a field update, i.e. the old property was
 * deprecated (a replacement is under way) while the Master keeps fallback-reading it, so the
 * property may only disappear once that deprecation window has passed.
 *
 * @author Mikhail Polivakha
 */
object PropertyRemovalMustFollowDeprecation : StatefulContractInvariant {

    override fun check(baseline: ContractDocument?, current: ContractDocument): List<String> {
        if (baseline == null) {
            // a document born now removed nothing: no released peer has ever seen it
            return emptyList()
        }

        val survivingProperties = current.properties
            .map { property -> property.schemaName to property.name }
            .toSet()

        return baseline.properties.mapNotNull { removed ->
            if (removed.schemaName to removed.name in survivingProperties) {
                // proeprty was not removed in the current version of contract - no action for this check
                return@mapNotNull null
            }

            val deprecated = baseline.markerVersion(removed.node, DEPRECATED_IN)
            val masterProduced = removed.schemaName in baseline.masterProducedSchemas

            if (deprecated == null) {
                if (masterProduced) {
                    return@mapNotNull ("${removed.location} is produced by the Master and was removed without prior "
                            + "deprecation against the released contract (${baseline.axelixVersion}): "
                            + "deprecate the property and keep producing it until the window passes before "
                            + "removing it")
                } else {
                    // a pure removal of a starter-only-produced property is immediate
                    return@mapNotNull null;
                }
            } else {
                if (!current.windowPassed(deprecated)) {
                    return@mapNotNull ("${removed.location} was removed while its deprecation window (deprecated in "
                            + "$deprecated) has not passed yet: keep it in the contract until the window "
                            + "closes")
                } else {
                    return@mapNotNull null;
                }
            }
        }
    }
}
