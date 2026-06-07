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
package com.axelixlabs.axelix.master.filter;

import org.jspecify.annotations.NonNull;

import org.springframework.util.StringUtils;

/**
 * Utility class that validates the provided path (typically end-user supplied) to make sure it is safe to return.
 *
 * @author Mikhail Polivakha
 */
public class SpaStaticResourcePathSanitizer {

    /**
     * @param contextPath the path to check
     * @return whether the path is considered safe or not.
     */
    public static boolean isSafe(@NonNull String contextPath) {
        String relativePath = StringUtils.trimLeadingCharacter(contextPath, '/');

        if (containsUnsafePathTokens(relativePath)) {
            return false;
        }

        String cleanedRelativePath = StringUtils.cleanPath(relativePath);

        return !isUnsafeCleanedPath(relativePath, cleanedRelativePath);
    }

    private static boolean containsUnsafePathTokens(String relativePath) {
        return !StringUtils.hasText(relativePath)
                || relativePath.contains("\\")
                || relativePath.contains(":")
                || relativePath.contains("//");
    }

    private static boolean isUnsafeCleanedPath(String relativePath, String cleanedRelativePath) {
        return !StringUtils.hasText(cleanedRelativePath)
                || cleanedRelativePath.startsWith("../")
                || cleanedRelativePath.startsWith("/")
                || !cleanedRelativePath.equals(relativePath);
    }
}
