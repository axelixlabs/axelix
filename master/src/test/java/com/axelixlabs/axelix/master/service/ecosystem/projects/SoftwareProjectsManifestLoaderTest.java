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

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.Ecosystem;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.ProjectReference;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProjectId;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.Succession;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SupportStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link SoftwareProjectsManifestLoader}.
 *
 * @author Mikhail Polivakha
 */
class SoftwareProjectsManifestLoaderTest {

    @Test
    void loadsManifestsInFilenameOrderMappingEveryField() {
        // given
        SoftwareProjectsManifestLoader subject = loaderOf("classpath*:axelix/dependencies-test/*.yaml");

        // when
        List<SoftwareProject> projects = subject.load();

        // then
        SoftwareProject sleuth = new SoftwareProject(
                SoftwareProjectId.of("spring-cloud-sleuth"),
                "Spring Cloud Sleuth",
                Ecosystem.OBSERVABILITY,
                SupportStatus.SUNSET,
                "Discontinued after the 3.1.x line. Tracing moved into Micrometer Tracing, which Spring Boot "
                        + "autoconfigures from 3.0 onward.",
                Set.of(
                        Library.of("org.springframework.cloud", "spring-cloud-sleuth-core"),
                        Library.of("org.springframework.cloud", "spring-cloud-sleuth-api")),
                Succession.supersededBy("io.micrometer:micrometer-tracing-bridge-brave"),
                ProjectReference.of(
                        "Sleuth to Micrometer Tracing migration", "https://example.invalid/sleuth-migration"));

        SoftwareProject micrometer = new SoftwareProject(
                SoftwareProjectId.of("micrometer"),
                "Micrometer",
                Ecosystem.OBSERVABILITY,
                SupportStatus.ACTIVE,
                "Actively developed.",
                Set.of(Library.of("io.micrometer", "micrometer-core")),
                null,
                ProjectReference.of("Micrometer releases", "https://example.invalid/micrometer-releases"));

        SoftwareProject ehcache2 = new SoftwareProject(
                SoftwareProjectId.of("ehcache2"),
                "Ehcache 2",
                Ecosystem.PERSISTENCE,
                SupportStatus.MAINTENANCE,
                "The 2.x line is closed. Development moved to org.ehcache:ehcache 3.x.",
                Set.of(Library.of("net.sf.ehcache", "ehcache")),
                Succession.noDirectReplacement(
                        "Replacing it is an architectural choice, the 3.x API is not compatible"),
                ProjectReference.of("Ehcache 3 migration guide", "https://example.invalid/ehcache-migration"));

        assertThat(projects).containsExactly(sleuth, micrometer, ehcache2);
    }

    @Test
    void refusesManifestWithUnknownProperty() {
        // given
        SoftwareProjectsManifestLoader subject = loaderOf("classpath:axelix/dependencies-broken/typo.yaml");

        // when / then
        assertThatThrownBy(subject::load)
                .isInstanceOf(SoftwareProjectsCatalogException.class)
                .hasMessageContaining("Failed to read the software projects manifest");
    }

    @Test
    void refusesManifestWithVersionedCoordinates() {
        // given
        SoftwareProjectsManifestLoader subject = loaderOf("classpath:axelix/dependencies-broken/versioned.yaml");

        // when / then
        assertThatThrownBy(subject::load)
                .isInstanceOf(SoftwareProjectsCatalogException.class)
                .hasMessageContaining("Failed to read the software projects manifest")
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("com.acme:versioned:1.2.3");
    }

    private static SoftwareProjectsManifestLoader loaderOf(String locationPattern) {
        return new SoftwareProjectsManifestLoader(new PathMatchingResourcePatternResolver(), locationPattern);
    }
}
