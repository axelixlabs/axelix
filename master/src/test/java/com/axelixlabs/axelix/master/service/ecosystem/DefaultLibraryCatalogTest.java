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

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.domain.ecosystem.Reference;
import com.axelixlabs.axelix.master.domain.ecosystem.SupportStatus;
import com.axelixlabs.axelix.master.domain.ecosystem.libraries.ArtifactCoordinates;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.Ecosystem;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProjectId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultLibraryCatalog}, covering the invariants of the curated data that it refuses to be
 * built without.
 *
 * @author Mikhail Polivakha
 */
class DefaultLibraryCatalogTest {

    @Test
    void refusesTwoEntriesSharingAnId() {
        // given.
        List<SoftwareProject> duplicated = List.of(
                library("jackson", "com.fasterxml.jackson.core:jackson-core"), library("jackson", "com.acme:x"));

        // when, then.
        assertThatThrownBy(() -> new DefaultLibraryCatalog(duplicated))
                .isInstanceOf(LibraryCatalogException.class)
                .hasMessageContaining("Duplicate library id 'jackson'");
    }

    @Test
    void refusesTwoEntriesClaimingTheSameArtifact() {
        // given. A real curation mistake: the artifact was moved to a new entry but left on the old one.
        List<SoftwareProject> overlapping = List.of(
                library("sleuth", "org.springframework.cloud:spring-cloud-sleuth-core"),
                library("micrometer-tracing", "org.springframework.cloud:spring-cloud-sleuth-core"));

        // when, then.
        assertThatThrownBy(() -> new DefaultLibraryCatalog(overlapping))
                .isInstanceOf(LibraryCatalogException.class)
                .hasMessageContaining("org.springframework.cloud:spring-cloud-sleuth-core")
                .hasMessageContaining("claimed by both");
    }

    @Test
    void refusesAnEntryThatNothingCouldEverMatch() {
        // when, then.
        assertThatThrownBy(() -> new SoftwareProject(
                        SoftwareProjectId.of("ghost"),
                        "Ghost",
                        Ecosystem.OTHER,
                        SupportStatus.ACTIVE,
                        "Actively developed.",
                        Set.of(),
                        null,
                        Reference.of("Ghost", "https://example.invalid/ghost")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("declares no coordinates");
    }

    @Test
    void exposesEveryEntryInDeclarationOrder() {
        // given.
        LibraryCatalog catalog =
                new DefaultLibraryCatalog(List.of(library("first", "com.acme:a"), library("second", "com.acme:b")));

        // when, then.
        assertThat(catalog.all()).extracting(entry -> entry.id().value()).containsExactly("first", "second");
    }

    private static SoftwareProject library(String id, String coordinates) {
        return new SoftwareProject(
                SoftwareProjectId.of(id),
                id,
                Ecosystem.OTHER,
                SupportStatus.ACTIVE,
                "Actively developed.",
                Set.of(ArtifactCoordinates.parse(coordinates)),
                null,
                Reference.of(id, "https://example.invalid/" + id));
    }
}
