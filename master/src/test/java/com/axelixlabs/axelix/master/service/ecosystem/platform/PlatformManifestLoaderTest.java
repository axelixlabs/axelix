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
package com.axelixlabs.axelix.master.service.ecosystem.platform;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformReleaseLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PlatformManifestLoader}.
 *
 * @author Mikhail Polivakha
 */
class PlatformManifestLoaderTest {

    @Test
    void loadsManifestsInFilenameOrderMappingEveryField() {
        // given
        PlatformManifestLoader subject = loaderOf("classpath*:axelix/platforms/valid/*.yaml");

        // when
        List<Platform> platforms = subject.load();

        // then
        assertThat(platforms).hasSize(2);

        Platform openJdk = platforms.get(0);
        assertThat(openJdk.name()).isEqualTo(PlatformName.OPEN_JDK);
        assertThat(openJdk.lines())
                .containsExactly(
                        new PlatformReleaseLine("21.0.x", LocalDate.of(2023, 9, 19), LocalDate.of(2028, 9, 30), null));

        Platform springBoot = platforms.get(1);
        assertThat(springBoot.name()).isEqualTo(PlatformName.SPRING_BOOT);
        assertThat(springBoot.lines())
                .containsExactly(
                        new PlatformReleaseLine(
                                "3.2.x",
                                LocalDate.of(2023, 11, 23),
                                LocalDate.of(2024, 12, 31),
                                LocalDate.of(2025, 12, 31)),
                        new PlatformReleaseLine(
                                "3.3.x",
                                LocalDate.of(2024, 5, 23),
                                LocalDate.of(2025, 6, 30),
                                LocalDate.of(2026, 6, 30)));
    }

    @Test
    void refusesManifestWithUnknownProperty() {
        // given
        PlatformManifestLoader subject = loaderOf("classpath:axelix/platforms/broken/typo.yaml");

        // when / then
        assertThatThrownBy(subject::load)
                .isInstanceOf(PlatformCatalogException.class)
                .hasMessageContaining("Failed to read the platform manifest");
    }

    @Test
    void refusesManifestDeclaringUnknownPlatform() {
        // given
        PlatformManifestLoader subject = loaderOf("classpath:axelix/platforms/broken/unknown-platform.yaml");

        // when / then
        assertThatThrownBy(subject::load)
                .isInstanceOf(PlatformCatalogException.class)
                .hasMessageContaining("Failed to read the platform manifest")
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quarkus");
    }

    @Test
    void loadsTheShippedManifestsFromTheDefaultLocation() {
        // given
        PlatformManifestLoader subject = new PlatformManifestLoader();

        // when
        List<Platform> platforms = subject.load();

        // then
        assertThat(platforms)
                .map(Platform::name)
                .contains(PlatformName.SPRING_BOOT)
                .doesNotContain(PlatformName.OPEN_JDK);
    }

    private static PlatformManifestLoader loaderOf(String locationPattern) {
        return new PlatformManifestLoader(new PathMatchingResourcePatternResolver(), locationPattern);
    }
}
