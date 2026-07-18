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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.net.URL;

/**
 * Provides information about the location from which the library is loaded.
 *
 * @author Ilya Naumov
 */
public class LibraryLocationProvider {

    /**
     * Returns whether the library is loaded from a file-based location.
     *
     * @return {@code true} if the library's code source uses the {@code file} protocol
     */
    public boolean hasFileProtocol() {
        URL libraryLocation = LibraryLocationProvider.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();
        return "file".equals(libraryLocation.getProtocol());
    }
}
