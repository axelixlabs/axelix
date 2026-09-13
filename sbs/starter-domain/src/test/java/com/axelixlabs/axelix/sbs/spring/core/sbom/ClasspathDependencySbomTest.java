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
package com.axelixlabs.axelix.sbs.spring.core.sbom;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ClasspathDependencySbom}.
 *
 * @author Mikhail Polivakha
 */
class ClasspathDependencySbomTest {

    /**
     * The test-classpath stand-in for the resource the Axelix build plugins package into the archive.
     */
    private static final String PRESENT_RESOURCE = "axelix/sbom-test/dependencies.cdx.json";

    @Test
    void shouldReadTheSbomWhenTheResourceIsPresent() {
        // given
        ClasspathDependencySbom sbom = new ClasspathDependencySbom(PRESENT_RESOURCE);

        // when
        Optional<byte[]> read = sbom.read();

        // then
        assertThat(read).isPresent();
        assertThat(new String(read.get(), StandardCharsets.UTF_8))
                .contains("\"bomFormat\": \"CycloneDX\"")
                .contains("\"specVersion\": \"1.6\"");
    }

    @Test
    void shouldReturnEmptyWhenTheResourceIsAbsent() {
        // given
        ClasspathDependencySbom sbom = new ClasspathDependencySbom("axelix/sbom-test/no-such-file.cdx.json");

        // when
        Optional<byte[]> read = sbom.read();

        // then
        assertThat(read).isEmpty();
    }
}
