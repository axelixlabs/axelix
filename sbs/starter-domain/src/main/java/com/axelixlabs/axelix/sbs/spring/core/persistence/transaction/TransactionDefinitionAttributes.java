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
package com.axelixlabs.axelix.sbs.spring.core.persistence.transaction;

/**
 * The static transaction attributes declared on a {@code @Transactional} method: its propagation behavior,
 * isolation level and read-only flag. These are discovered once, at bean post-processing time, from the
 * {@code @Transactional} annotation, and never change over the lifetime of the application.
 *
 * <p>Propagation and isolation are kept as plain {@link String}s (the {@code Propagation}/{@code Isolation} enum
 * names) so that this module does not have to depend on {@code spring-tx}.
 *
 * @author Mikhail Polivakha
 */
public final class TransactionDefinitionAttributes {

    private final String propagation;
    private final String isolation;
    private final boolean readOnly;

    public TransactionDefinitionAttributes(String propagation, String isolation, boolean readOnly) {
        this.propagation = propagation;
        this.isolation = isolation;
        this.readOnly = readOnly;
    }

    public String getPropagation() {
        return propagation;
    }

    public String getIsolation() {
        return isolation;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
