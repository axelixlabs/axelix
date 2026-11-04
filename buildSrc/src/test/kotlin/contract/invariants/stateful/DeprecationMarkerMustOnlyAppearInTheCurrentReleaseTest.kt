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

    private fun baseline(): List<ContractSchema> =
        ContractSchema.parse(document("released-baseline.yaml"), RELEASED_VERSION)

    private fun current(name: String): List<ContractSchema> =
        ContractSchema.parse(document(name), CURRENT_VERSION)

    private fun document(name: String): File =
        File(javaClass.getResource(
            "/contract/stateful/${javaClass.simpleName.removeSuffix("Test")}/$name")!!.toURI())
}
