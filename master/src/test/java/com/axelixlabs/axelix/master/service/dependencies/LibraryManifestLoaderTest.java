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

import org.junit.jupiter.api.Test;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.axelixlabs.axelix.master.domain.dependencies.ArtifactCoordinates;
import com.axelixlabs.axelix.master.domain.dependencies.Ecosystem;
import com.axelixlabs.axelix.master.domain.dependencies.KnownLibrary;
import com.axelixlabs.axelix.master.domain.dependencies.LibraryId;
import com.axelixlabs.axelix.master.domain.dependencies.Succession;
import com.axelixlabs.axelix.master.domain.dependencies.SupportStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link LibraryManifestLoader}, reading the fixture manifests under
 * {@code axelix/dependencies-test}.
 *
 * @author Mikhail Polivakha
 */
class LibraryManifestLoaderTest {

    private static final String FIXTURES = "classpath*:axelix/dependencies-test/*.yaml";

    private final LibraryManifestLoader loader =
            new LibraryManifestLoader(new PathMatchingResourcePatternResolver(), FIXTURES);

    @Test
    void loadsEveryManifestAndStampsTheEcosystemOfItsFile() {
        // when.
        List<KnownLibrary> libraries = loader.load();

        // then. Filename order: observability.yaml before persistence.yaml
        assertThat(libraries)
                .extracting(library -> library.id().value())
                .containsExactly("spring-cloud-sleuth", "micrometer", "ehcache2");

        assertThat(libraries)
                .extracting(KnownLibrary::ecosystem)
                .containsExactly(Ecosystem.OBSERVABILITY, Ecosystem.OBSERVABILITY, Ecosystem.PERSISTENCE);
    }

    @Test
    void bindsEveryCuratedFieldOfAnEntry() {
        // when.
        KnownLibrary sleuth = loader.load().getFirst();

        // then.
        assertThat(sleuth.displayName()).isEqualTo("Spring Cloud Sleuth");
        assertThat(sleuth.status()).isEqualTo(SupportStatus.SUNSET);
        assertThat(sleuth.summary()).startsWith("Discontinued after the 3.1.x line.");

        assertThat(sleuth.coordinates())
                .containsExactlyInAnyOrder(
                        ArtifactCoordinates.of("org.springframework.cloud", "spring-cloud-sleuth-core"),
                        ArtifactCoordinates.of("org.springframework.cloud", "spring-cloud-sleuth-api"));

        assertThat(sleuth.succession())
                .isEqualTo(Succession.supersededBy("io.micrometer:micrometer-tracing-bridge-brave"));

        assertThat(sleuth.reference().label()).isEqualTo("Sleuth to Micrometer Tracing migration");
        assertThat(sleuth.reference().url()).hasToString("https://example.invalid/sleuth-migration");
    }

    @Test
    void leavesSuccessionAbsentForAnActiveProject() {
        // when.
        KnownLibrary micrometer = loader.load().get(1);

        // then.
        assertThat(micrometer.status()).isEqualTo(SupportStatus.ACTIVE);
        assertThat(micrometer.succession()).isNull();
    }

    @Test
    void indexesEveryCoordinateOfAProjectOntoTheSameEntry() {
        // given.
        LibraryCatalog catalog = new DefaultLibraryCatalog(loader.load());

        // when, then. Both Sleuth artifacts resolve to the one curated project.
        assertThat(catalog.find(ArtifactCoordinates.of("org.springframework.cloud", "spring-cloud-sleuth-core")))
                .hasValueSatisfying(library -> assertThat(library.id()).isEqualTo(LibraryId.of("spring-cloud-sleuth")));

        assertThat(catalog.find(ArtifactCoordinates.of("org.springframework.cloud", "spring-cloud-sleuth-api")))
                .hasValueSatisfying(library -> assertThat(library.id()).isEqualTo(LibraryId.of("spring-cloud-sleuth")));
    }

    @Test
    void reportsAnUncuratedArtifactAsAbsent() {
        // given.
        LibraryCatalog catalog = new DefaultLibraryCatalog(loader.load());

        // when, then.
        assertThat(catalog.find(ArtifactCoordinates.of("com.acme", "nothing-we-know-about")))
                .isEmpty();
    }

    @Test
    void failsWhenAManifestHoldsAnUnknownProperty() {
        // given.
        LibraryManifestLoader broken = new LibraryManifestLoader(
                new PathMatchingResourcePatternResolver(), "classpath*:axelix/dependencies-broken/typo.yaml");

        // when, then.
        assertThatThrownBy(broken::load)
                .isInstanceOf(LibraryCatalogException.class)
                .hasMessageContaining("Failed to read the library manifest");
    }

    @Test
    void failsWhenAManifestWritesCoordinatesWithAVersion() {
        // given.
        LibraryManifestLoader broken = new LibraryManifestLoader(
                new PathMatchingResourcePatternResolver(), "classpath*:axelix/dependencies-broken/versioned.yaml");

        // when, then.
        assertThatThrownBy(broken::load)
                .isInstanceOf(LibraryCatalogException.class)
                .rootCause()
                .hasMessageContaining("groupId:artifactId");
    }
}
