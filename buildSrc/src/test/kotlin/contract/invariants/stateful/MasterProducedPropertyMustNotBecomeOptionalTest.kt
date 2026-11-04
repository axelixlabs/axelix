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
        val problems = check(baseline(), current("unchanged.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a born document is not judged`() {
        val problems = check(null, current("born-document.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a withdrawn required of a starter-produced property passes`() {
        val problems = check(baseline(), current("starter-produced-required-withdrawn.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a withdrawn required of a master-produced property fails`() {
        val problems = check(baseline(), current("required-withdrawn.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.configuredLevel' is produced by the Master and is "
                    + "'required' in the released contract (1.1.0): withdrawing 'required' is a hidden "
                    + "removal, deprecate the property instead and keep it required until the window passes"),
            problems)
    }

    /**
     * The fixture bundles several schemas. Pair them the way the validator does: by name.
     */
    private fun check(baseline: List<ContractSchema>?, current: List<ContractSchema>): List<String> {
        val baselineByName = baseline.orEmpty().associateBy { schema -> schema.name }
        val currentByName = current.associateBy { schema -> schema.name }
        val names = (current.map { schema -> schema.name } + baseline.orEmpty().map { schema -> schema.name }).distinct()
        return names.flatMap { name ->
            MasterProducedPropertyMustNotBecomeOptional.check(baselineByName[name], currentByName[name])
        }
    }

    private fun baseline(): List<ContractSchema> =
        ContractSchema.parse(document("released-baseline.yaml"), RELEASED_VERSION)

    private fun current(name: String): List<ContractSchema> =
        ContractSchema.parse(document(name), CURRENT_VERSION)

    private fun document(name: String): File =
        File(javaClass.getResource(
            "/contract/stateful/${javaClass.simpleName.removeSuffix("Test")}/$name")!!.toURI())
}
