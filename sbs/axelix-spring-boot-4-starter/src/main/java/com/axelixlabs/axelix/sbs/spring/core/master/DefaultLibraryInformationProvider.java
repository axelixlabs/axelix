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

import org.jspecify.annotations.Nullable;

import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.util.ClassUtils;

import static com.axelixlabs.axelix.sbs.spring.core.utils.StringUtils.emptyIfNull;

/**
 * Default implementation {@link LibraryInformationProvider}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultLibraryInformationProvider implements LibraryInformationProvider {

    private static final ClassLoader CLASS_LOADER = DefaultLibraryInformationProvider.class.getClassLoader();

    private static final boolean KOTLIN_VERSION_PRESENT = ClassUtils.isPresent("kotlin.KotlinVersion", CLASS_LOADER);

    @Override
    public @Nullable String getKotlinVersion() {
        if (!KOTLIN_VERSION_PRESENT) {
            return null;
        }

        try {
            Class<?> kotlinVersionClass = Class.forName("kotlin.KotlinVersion", false, CLASS_LOADER);
            Object current = kotlinVersionClass.getField("CURRENT").get(null);
            return String.valueOf(current);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Override
    public String getSpringBootVersion() {
        return emptyIfNull(SpringBootVersion.getVersion());
    }

    @Override
    public String getSpringVersion() {
        return emptyIfNull(SpringVersion.getVersion());
    }
}
