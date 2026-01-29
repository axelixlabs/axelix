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
package com.axelixlabs.axelix.sbs.spring.master;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;

/**
 * Unit test for {@link CycloneDXSBOMLibraryDiscoverer}.
 *
 * @author Mikhail Polivakha
 */
class CycloneDXSBOMLibraryDiscovererTest {

    private CycloneDXSBOMLibraryDiscoverer subject;

    @BeforeEach
    void setUp() {
        subject = new CycloneDXSBOMLibraryDiscoverer(new ClassPathResource("other/application.cdx.json"));
    }

    @Test
    void shouldFindSpringBootVersion() {
        // when
        Optional<String> libraryVersion = subject.getLibraryVersion("spring-boot", "org.springframework.boot");

        // then
        Assertions.assertThat(libraryVersion).isPresent().hasValue("3.5.0");
    }

    @Test
    void shouldReturnEmptyOptionalOnNonExistentLibrary() {
        // when
        Optional<String> libraryVersion = subject.getLibraryVersion("some-artifact", "some-group");

        // then
        Assertions.assertThat(libraryVersion).isEmpty();
    }
}
