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
package com.axelixlabs.axelix.common.domain;

/**
 * The link to the class that is loaded by the application.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class LoadedClass {

    /**
     * The link to class loader that loaded that particular class.
     */
    private ClassLoader classLoader;

    /**
     * Fully qualified class name.
     */
    private String fqcn;

    /**
     * The class-path-entry from which the given class is loaded.
     */
    private ClassPathEntry classPathEntry;

    public LoadedClass(ClassLoader classLoader, String fqcn, ClassPathEntry classPathEntry) {
        this.classLoader = classLoader;
        this.fqcn = fqcn;
        this.classPathEntry = classPathEntry;
    }
}
