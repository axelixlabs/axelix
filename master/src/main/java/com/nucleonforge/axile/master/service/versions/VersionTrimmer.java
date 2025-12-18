/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.service.versions;

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
