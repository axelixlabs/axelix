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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformReleaseLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultPlatformCatalog}.
 *
 * @author Mikhail Polivakha
 */
@ExtendWith(MockitoExtension.class)
class DefaultPlatformCatalogTest {

    @Mock
    private PlatformManifestLoader platformManifestLoader;

    @Test
    void findsLoadedPlatformByItsName() {
        // given
        Platform springBoot = platform(PlatformName.SPRING_BOOT);
        Platform openJdk = platform(PlatformName.OPEN_JDK);
        when(platformManifestLoader.load()).thenReturn(List.of(springBoot, openJdk));

        // when
        DefaultPlatformCatalog subject = new DefaultPlatformCatalog(platformManifestLoader);

        // then
        assertThat(subject.find(PlatformName.SPRING_BOOT)).contains(springBoot);
        assertThat(subject.find(PlatformName.OPEN_JDK)).contains(openJdk);
    }

    @Test
    void returnsEmptyOptionalForPlatformAbsentInManifests() {
        // given
        when(platformManifestLoader.load()).thenReturn(List.of(platform(PlatformName.SPRING_BOOT)));

        // when
        DefaultPlatformCatalog subject = new DefaultPlatformCatalog(platformManifestLoader);

        // then
        assertThat(subject.find(PlatformName.OPEN_JDK)).isEmpty();
    }

    @Test
    void rejectsPlatformDeclaredByTwoManifests() {
        // given
        when(platformManifestLoader.load())
                .thenReturn(List.of(platform(PlatformName.SPRING_BOOT), platform(PlatformName.SPRING_BOOT)));

        // when / then
        assertThatThrownBy(() -> new DefaultPlatformCatalog(platformManifestLoader))
                .isInstanceOf(PlatformCatalogException.class)
                .hasMessage("Platform 'SPRING_BOOT' is declared by two manifests");
    }

    private static Platform platform(PlatformName name) {
        PlatformReleaseLine line =
                new PlatformReleaseLine("3.2.x", LocalDate.of(2023, 11, 23), LocalDate.of(2024, 12, 31), null);

        return new Platform(name, List.of(line));
    }
}
