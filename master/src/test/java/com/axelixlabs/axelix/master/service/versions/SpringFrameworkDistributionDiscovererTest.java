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
package com.axelixlabs.axelix.master.service.versions;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.domain.BuildInfo;
import com.axelixlabs.axelix.common.domain.JarClassPathEntry;
import com.axelixlabs.axelix.master.model.software.SoftwareDistribution;

import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createBuildInfo;

/**
 * Unit test for {@link SpringFrameworkDistributionDiscoverer}.
 *
 * @author Mikhail Polivakha
 */
class SpringFrameworkDistributionDiscovererTest {

    private SpringFrameworkDistributionDiscoverer distributionDiscoverer;

    @BeforeEach
    void setUp() {
        distributionDiscoverer = new SpringFrameworkDistributionDiscoverer();
    }

    /**
     * Currently, it is not going to be possible to not discover spring framework in
     * the service, but later in the future it maybe will be possible.
     */
    @Test
    void testDiscoverNoSpringFramework() {
        // given.
        BuildInfo instance = createBuildInfo(
                new JarClassPathEntry("org.hibernate", "hibernate-core", "6.1.7", null, null),
                new JarClassPathEntry("org.jboss", "jandex", "2.4.2", null, null),
                new JarClassPathEntry("org.jetbrains.kotlin", "kotlin-stdlib", "2.0.17", null, null));

        // when.
        SoftwareDistribution discovered = distributionDiscoverer.discover(instance);

        // then.
        Assertions.assertThat(discovered).isNull();
    }

    @Test
    void testDiscoverHappyPath() {
        // given.
        String springVersion = "6.1.2";
        BuildInfo instance = createBuildInfo(
                new JarClassPathEntry("org.hibernate", "hibernate-core", "6.1.7", null, Set.of()),
                new JarClassPathEntry("org.jboss", "jandex", "2.4.2", null, Set.of()),
                new JarClassPathEntry("org.springframework", "spring-core", springVersion, null, Set.of()),
                new JarClassPathEntry("org.jetbrains.kotlin", "kotlin-stdlib", "2.0.17", null, Set.of()));

        // when.
        SoftwareDistribution discovered = distributionDiscoverer.discover(instance);

        // then.
        Assertions.assertThat(discovered).isNotNull();
        Assertions.assertThat(discovered.version()).isEqualTo(springVersion);
    }
}
