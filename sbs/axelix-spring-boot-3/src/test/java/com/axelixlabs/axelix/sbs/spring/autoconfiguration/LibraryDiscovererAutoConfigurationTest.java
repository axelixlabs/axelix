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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;

import com.axelixlabs.axelix.sbs.spring.core.master.CycloneDXSBOMLibraryDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.master.LibraryDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.master.NoOpLibraryDiscoverer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link LibraryDiscovererAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
 */
class LibraryDiscovererAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LibraryDiscovererAutoConfiguration.class));

    @Test
    void shouldCreateCycloneDXLibraryDiscoverer_whenResourcePresentInClasspath() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(LibraryDiscoverer.class);
            assertThat(context.getBean(LibraryDiscoverer.class))
                    .isExactlyInstanceOf(CycloneDXSBOMLibraryDiscoverer.class);
            assertThat(context).doesNotHaveBean(NoOpLibraryDiscoverer.class);
        });
    }

    @Test
    void shouldCreateCycloneDXLibraryDiscoverer_whenExplicitPropertyProvided() {
        contextRunner
                .withPropertyValues("axelix.sbom.cyclonedx.location=/other/application.cdx.json")
                .run(context -> {
                    assertThat(context).hasSingleBean(LibraryDiscoverer.class);
                    assertThat(context.getBean(LibraryDiscoverer.class))
                            .isExactlyInstanceOf(CycloneDXSBOMLibraryDiscoverer.class);
                    assertThat(context).doesNotHaveBean(NoOpLibraryDiscoverer.class);
                });
    }

    @Test
    void shouldCreateNoOpLibraryDiscoverer_whenNeitherResourceNorPropertyPresent() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(new ClassPathResource("META-INF/sbom/application.cdx.json")))
                .run(context -> {
                    assertThat(context).hasSingleBean(LibraryDiscoverer.class);
                    assertThat(context.getBean(LibraryDiscoverer.class))
                            .isExactlyInstanceOf(NoOpLibraryDiscoverer.class);
                    assertThat(context).doesNotHaveBean(CycloneDXSBOMLibraryDiscoverer.class);
                });
    }
}
