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
package com.nucleonforge.axelix.common.api.transform.units;

import java.util.Set;

import com.nucleonforge.axelix.common.api.transform.BaseUnitValueTransformer;
import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoints;

/**
 * The base unit that is reported by {@link ActuatorEndpoints#GET_SINGLE_METRIC} which we
 * know and care about. By "care" I mean that we can apply certain transformations for the
 * values that are reported by this base unit.
 * <p>
 * For instance, if the value of the given metric is
 * 123456789, and the base unit is {@link BytesMemoryBaseUnit bytes}, then we may be
 * willing to convert it onto approximately 117.7 MB (123456789 divided by 1024^2).
 * <p>
 * This "transformation" mechanism example above exists mostly for readability purposes.
 * The actual convertion is implemented via {@link BaseUnitValueTransformer}.
 *
 * @see BaseUnitValueTransformer
 * @author Mikhail Polivakha
 */
public interface BaseUnit {

    /**
     * @return An array of aliases for the given base unit. All members expected to be in lower case.
     */
    Set<String> getAliases();

    /**
     * @return The name of the base unit as to be displayed on the UI.
     */
    String getDisplayName();
}
