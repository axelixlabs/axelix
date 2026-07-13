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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.core.SpringVersion;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DefaultJarLauncherDetector}.
 *
 * @author Ilya Naumov
 */
public class DefaultJarLauncherDetectorTest {
    final DefaultJarLauncherDetector detector = new DefaultJarLauncherDetector(SpringVersion.class);

    @ParameterizedTest // GH-1219
    @ValueSource(
            strings = {
                "org.springframework.boot.loader.LaunchedURLClassLoader",
                "org.springframework.boot.loader.launch.LaunchedClassLoader"
            })
    void returnTrue_whenClassLoaderNameMatches(String classLoaderName) {
        // when.
        boolean result = detector.isClassLoaderNameMatches(classLoaderName);

        // then.
        assertThat(result).isEqualTo(true);
    }

    @ParameterizedTest // GH-1219
    @ValueSource(
            strings = {"org.springframework.boot.loader.ClassLoader", "org.springframework.boot.LaunchedClassLoader", ""
            })
    void returnFalse_whenClassLoaderNameDoesNotMatch(String classLoaderName) {
        // when.
        boolean result = detector.isClassLoaderNameMatches(classLoaderName);

        // then.
        assertThat(result).isEqualTo(false);
    }
}
