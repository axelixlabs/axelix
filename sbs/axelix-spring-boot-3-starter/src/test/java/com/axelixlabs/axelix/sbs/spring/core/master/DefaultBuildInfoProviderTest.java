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
package com.axelixlabs.axelix.sbs.spring.core.master;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultBuildInfoProvider}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
@Import({DefaultBuildInfoProvider.class, ProjectInfoAutoConfiguration.class})
@TestPropertySource(properties = "spring.info.build.location=classpath:other/build-info.properties")
class DefaultBuildInfoProviderTest {

    @Autowired
    private DefaultBuildInfoProvider subject;

    @Test
    void shouldAssembleBuildInfoFromPropertiesFile() {
        // when. / then.
        assertThat(subject.getGroup()).isEqualTo("com.axelixlabs.axelix");
        assertThat(subject.getArtifact()).isEqualTo("petclinic");
        assertThat(subject.getVersion()).isEqualTo("1.0.0-SNAPSHOT");
    }

    @Test
    void shouldReturnEmptyStringsWhenBuildInfoIsAbsent() {
        // given.
        DefaultBuildInfoProvider providerWithoutBuildInfo = new DefaultBuildInfoProvider(null);

        // when. / then.
        assertThat(providerWithoutBuildInfo.getGroup()).isEmpty();
        assertThat(providerWithoutBuildInfo.getArtifact()).isEmpty();
        assertThat(providerWithoutBuildInfo.getVersion()).isEmpty();
    }
}
