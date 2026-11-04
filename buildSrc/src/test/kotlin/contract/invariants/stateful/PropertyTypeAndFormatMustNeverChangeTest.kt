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
 * Unit tests for {@link PropertyTypeAndFormatMustNeverChange}.
 *
 * @author Mikhail Polivakha
 */
class PropertyTypeAndFormatMustNeverChangeTest {

    companion object {
        private const val CURRENT_VERSION = "1.2.0-SNAPSHOT"
        private const val RELEASED_VERSION = "1.1.0"
    }

    @Test
    fun `unchanged types and formats pass`() {
        val problems = PropertyTypeAndFormatMustNeverChange
            .check(baseline(), current("unchanged.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a born document is not judged`() {
        val problems = PropertyTypeAndFormatMustNeverChange
            .check(null, current("born-document.yaml"))

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `a redefined type of a released property fails`() {
        val problems = PropertyTypeAndFormatMustNeverChange
            .check(baseline(), current("type-redefined.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.configuredLevel' changed its 'type' from 'string' "
                    + "to 'integer' against the released contract (1.1.0): a released property must never "
                    + "be redefined in place, introduce a new property and deprecate the old one instead"),
            problems)
    }

    @Test
    fun `a redefined format of a released property fails`() {
        val problems = PropertyTypeAndFormatMustNeverChange
            .check(baseline(), current("format-redefined.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.ttlSeconds' changed its 'format' from 'int64' "
                    + "to 'int32' against the released contract (1.1.0): a released property must never "
                    + "be redefined in place, introduce a new property and deprecate the old one instead"),
            problems)
    }

    @Test
    fun `a dropped format of a released property fails`() {
        val problems = PropertyTypeAndFormatMustNeverChange
            .check(baseline(), current("format-dropped.yaml"))

        assertEquals(
            listOf(
                "the property 'LogLevelChangeRequest.ttlSeconds' changed its 'format' from 'int64' "
                    + "to nothing against the released contract (1.1.0): a released property must never "
                    + "be redefined in place, introduce a new property and deprecate the old one instead"),
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
