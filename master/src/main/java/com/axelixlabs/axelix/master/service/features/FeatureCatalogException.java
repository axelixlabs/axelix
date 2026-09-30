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
package com.axelixlabs.axelix.master.service.features;

/**
 * Thrown when the curated feature manifest cannot be located, read, parsed or validated. Raised at startup so a
 * malformed manifest fails the boot instead of surfacing at request time.
 *
 * @author Mikhail Polivakha
 */
public class FeatureCatalogException extends RuntimeException {

    public FeatureCatalogException(String message) {
        super(message);
    }

    public FeatureCatalogException(String message, Throwable cause) {
        super(message, cause);
    }
}
