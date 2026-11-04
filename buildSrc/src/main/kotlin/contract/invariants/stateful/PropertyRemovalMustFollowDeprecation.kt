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
package contract.invariants.stateful

import contract.ContractSchema
import contract.ContractSchema.Companion.DEPRECATED_IN

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

    override fun check(baseline: ContractSchema?, current: ContractSchema?): List<String> {
        if (baseline == null || current == null) {
            return emptyList()
        }

        val survivingProperties = current.properties.map { property -> property.name }.toSet()

        return baseline.properties.mapNotNull { removed ->
            if (removed.name in survivingProperties) {
                return@mapNotNull null
            }

            val deprecated = baseline.markerVersion(removed.node, DEPRECATED_IN)

            if (deprecated == null) {
                if (baseline.producedBy == "starter") {
                    // a pure removal of a starter-produced property is immediate
                    return@mapNotNull null
                } else {
                    return@mapNotNull ("${removed.location} is produced by the Master and was removed without prior "
                            + "deprecation against the released contract (${baseline.axelixVersion}): "
                            + "deprecate the property and keep producing it until the window passes before "
                            + "removing it")
                }
            } else {
                if (!current.windowPassed(deprecated)) {
                    return@mapNotNull ("${removed.location} was removed while its deprecation window (deprecated in "
                            + "$deprecated) has not passed yet: keep it in the contract until the window "
                            + "closes")
                } else {
                    return@mapNotNull null
                }
            }
        }
    }
}
