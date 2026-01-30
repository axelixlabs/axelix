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
package com.axelixlabs.axelix.sbs.spring.env;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Default implementation {@link PropertyNameNormalizer}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class DefaultPropertyNameNormalizer implements PropertyNameNormalizer {

    @Override
    public String normalize(String propertyName) {
        return propertyName
                .replaceAll("(?<!\\d)0(?!\\d)", "") // removes the zero index like [0] --> []
                .replaceAll("[^A-Za-z0-9]", "")
                .toLowerCase();
    }

    @Override
    public <C extends Collection<String>> C normalizeAll(C propertyNames, Supplier<C> collectionFactory) {
        return propertyNames.stream().map(this::normalize).collect(Collectors.toCollection(collectionFactory));
    }
}
