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
package contract.invariants.stateless

import contract.ContractDocument
import contract.ContractDocument.Companion.INTRODUCED_IN

/**
 * Every schema and every property must declare the Axelix version it was introduced in.
 * The schema version is when the data model was born; the property version is what answers
 * "may the Master already rely on this property / drop the support of it". There is
 * deliberately no fallback from a property to its schema, or from a schema to the document.
 *
 * @author Mikhail Polivakha
 */
object IntroductionIsDeclared : StatelessContractInvariant {

    override fun check(document: ContractDocument): List<String> {
        val parts = document.schemas.map { schema -> ContractDocument.MarkedPart(schema.location, schema.node) } +
            document.properties.map { property -> ContractDocument.MarkedPart(property.location, property.node) }
        return parts
            .filter { part -> document.marker(part.node, INTRODUCED_IN) == null }
            .map { part -> "${part.location} is missing '$INTRODUCED_IN'" }
    }
}
