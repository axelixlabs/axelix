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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.DefaultResourceLoader;

import com.axelixlabs.axelix.master.domain.features.Feature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link InstanceFeaturesManifestLoader}.
 *
 * @author Mikhail Polivakha
 */
class InstanceFeaturesManifestLoaderTest {

    @Test
    void flattensEveryReleaseIntoAFeatureTaggedWithItsIntroducingVersion() {
        // given
        InstanceFeaturesManifestLoader subject = loaderOf("classpath:axelix/features/test/valid.yaml");

        // when
        List<Feature> features = subject.load();

        // then
        assertThat(features).extracting(Feature::id).containsExactly("always-on", "added-in-1-2", "added-in-2-0");
        assertThat(features)
                .extracting(feature -> feature.availableSince().majorMinor())
                .containsExactly("1.0", "1.2", "2.0");
    }

    @Test
    void rejectsAnUnknownProperty() {
        // given
        InstanceFeaturesManifestLoader subject = loaderOf("classpath:axelix/features/test/unknown-property.yaml");

        // when / then
        assertThatThrownBy(subject::load).isInstanceOf(FeatureCatalogException.class);
    }

    @Test
    void rejectsAMissingManifest() {
        // given
        InstanceFeaturesManifestLoader subject = loaderOf("classpath:axelix/features/test/does-not-exist.yaml");

        // when / then
        assertThatThrownBy(subject::load).isInstanceOf(FeatureCatalogException.class);
    }

    private static InstanceFeaturesManifestLoader loaderOf(String location) {
        return new InstanceFeaturesManifestLoader(new DefaultResourceLoader(), location);
    }
}
