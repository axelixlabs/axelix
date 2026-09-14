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
package com.axelixlabs.axelix.master.service.ecosystem.projects;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.Ecosystem;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.ProjectReference;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProjectId;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SupportStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultSoftwareProjectsCatalog}.
 *
 * @author Mikhail Polivakha
 */
@ExtendWith(MockitoExtension.class)
class DefaultSoftwareProjectsCatalogTest {

    @Mock
    private SoftwareProjectsManifestLoader softwareProjectsManifestLoader;

    @Test
    void resolvesOwningProjectByAnyOfItsLibraries() {
        // given
        SoftwareProject sleuth = project(
                "spring-cloud-sleuth",
                Library.of("org.springframework.cloud", "spring-cloud-sleuth-core"),
                Library.of("org.springframework.cloud", "spring-cloud-sleuth-api"));
        SoftwareProject micrometer = project("micrometer", Library.of("io.micrometer", "micrometer-core"));
        when(softwareProjectsManifestLoader.load()).thenReturn(List.of(sleuth, micrometer));

        // when
        DefaultSoftwareProjectsCatalog subject = new DefaultSoftwareProjectsCatalog(softwareProjectsManifestLoader);

        // then
        assertThat(subject.resolve(Library.of("org.springframework.cloud", "spring-cloud-sleuth-core")))
                .contains(sleuth);
        assertThat(subject.resolve(Library.of("org.springframework.cloud", "spring-cloud-sleuth-api")))
                .contains(sleuth);
        assertThat(subject.resolve(Library.of("io.micrometer", "micrometer-core")))
                .contains(micrometer);
    }

    @Test
    void returnsEmptyOptionalForLibraryNoProjectClaims() {
        // given
        when(softwareProjectsManifestLoader.load())
                .thenReturn(List.of(project("micrometer", Library.of("io.micrometer", "micrometer-core"))));

        // when
        DefaultSoftwareProjectsCatalog subject = new DefaultSoftwareProjectsCatalog(softwareProjectsManifestLoader);

        // then
        assertThat(subject.resolve(Library.of("com.acme", "unknown"))).isEmpty();
    }

    @Test
    void rejectsLibraryClaimedByTwoProjects() {
        // given
        Library contested = Library.of("com.acme", "shared");
        when(softwareProjectsManifestLoader.load())
                .thenReturn(List.of(project("first", contested), project("second", contested)));

        // when / then
        assertThatThrownBy(() -> new DefaultSoftwareProjectsCatalog(softwareProjectsManifestLoader))
                .isInstanceOf(SoftwareProjectsCatalogException.class)
                .hasMessage("Artifact 'com.acme:shared' is claimed by both 'first' and 'second'");
    }

    private static SoftwareProject project(String id, Library... libraries) {
        return new SoftwareProject(
                SoftwareProjectId.of(id),
                id,
                Ecosystem.OTHER,
                SupportStatus.ACTIVE,
                "Actively developed.",
                Set.of(libraries),
                null,
                ProjectReference.of("%s releases".formatted(id), "https://example.invalid/" + id));
    }
}
