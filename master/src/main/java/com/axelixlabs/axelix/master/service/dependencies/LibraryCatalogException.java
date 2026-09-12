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
package com.axelixlabs.axelix.master.service.dependencies;

/**
 * Raised when the curated catalog cannot be read or does not hold together, e.g. a malformed manifest or two entries
 * claiming the same artifact. The catalog is data Axelix ships, so any such failure is a defect in the distribution
 * rather than anything a user did, and it is meant to abort startup.
 *
 * @author Mikhail Polivakha
 */
public class LibraryCatalogException extends RuntimeException {

    public LibraryCatalogException(String message) {
        super(message);
    }

    public LibraryCatalogException(String message, Throwable cause) {
        super(message, cause);
    }
}
