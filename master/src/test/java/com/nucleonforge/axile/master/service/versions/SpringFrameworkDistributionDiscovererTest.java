/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.service.versions;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.domain.BuildInfo;
import com.nucleonforge.axile.common.domain.JarClassPathEntry;
import com.nucleonforge.axile.master.model.software.SoftwareDistribution;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createBuildInfo;

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
