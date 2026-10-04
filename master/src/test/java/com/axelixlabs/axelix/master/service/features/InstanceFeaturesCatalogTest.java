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
package com.axelixlabs.axelix.master.service.features;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.DefaultResourceLoader;

import com.axelixlabs.axelix.common.utils.SemanticVersion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link InstanceFeaturesCatalog}.
 *
 * @author Mikhail Polivakha
 */
class InstanceFeaturesCatalogTest {

    private final InstanceFeaturesCatalog subject = catalogOf("classpath:axelix/features/test/valid.yaml");

    @Test
    void reportsOnlyTheBaselineFeatureBelowTheFirstGate() {
        assertThat(subject.availableFor(SemanticVersion.parse("1.1.5"))).containsExactly("always-on");
    }

    @Test
    void reportsAFeatureFromTheReleaseItWasIntroducedIn() {
        // the patch and qualifier are irrelevant: a 1.2 pre-release already carries the 1.2 feature
        assertThat(subject.availableFor(SemanticVersion.parse("1.2.0-SNAPSHOT")))
                .containsExactly("always-on", "added-in-1-2");
    }

    @Test
    void reportsAFeatureForEveryLaterMinorRelease() {
        assertThat(subject.availableFor(SemanticVersion.parse("1.9.3"))).containsExactly("always-on", "added-in-1-2");
    }

    @Test
    void reportsFeaturesGatedToALaterMajorRelease() {
        assertThat(subject.availableFor(SemanticVersion.parse("2.0.1")))
                .containsExactly("always-on", "added-in-1-2", "added-in-2-0");
    }

    @Test
    void reportsNoFeatureBelowEveryGate() {
        assertThat(subject.availableFor(SemanticVersion.parse("0.9.0"))).isEmpty();
    }

    @Test
    void rejectsAManifestDeclaringAFeatureIdTwice() {
        assertThatThrownBy(() -> catalogOf("classpath:axelix/features/test/duplicate-id.yaml"))
                .isInstanceOf(FeatureCatalogException.class)
                .hasMessageContaining("always-on");
    }

    private static InstanceFeaturesCatalog catalogOf(String location) {
        return new InstanceFeaturesCatalog(new InstanceFeaturesManifestLoader(new DefaultResourceLoader(), location));
    }
}
