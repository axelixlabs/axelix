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
package com.axelixlabs.axelix.master.utils;

/**
 * Utility class for trimming software versions (e.g. major, major.minor parts.
 *
 * @since 18.12.2025
 * @author Nikita Kirillov
 */
public class VersionTrimmer {

    public static String getMajorVersion(String version) {
        if (version == null || version.isBlank()) {
            return version;
        }

        version = version.trim();
        int firstDotIndex = version.indexOf('.');

        if (firstDotIndex != -1) {
            return version.substring(0, firstDotIndex);
        }

        return version;
    }

    public static String getMajorMinorVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            return version;
        }

        version = version.trim();

        int firstDotIndex = version.indexOf('.');
        if (firstDotIndex == -1) {
            return version;
        }

        int secondDotIndex = version.indexOf('.', firstDotIndex + 1);
        if (secondDotIndex == -1) {
            return version;
        }

        return version.substring(0, secondDotIndex);
    }
}
