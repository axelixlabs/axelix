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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.domain.JarClassPathEntry;
import com.nucleonforge.axile.master.model.software.SoftwareDistribution;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createBuildInfo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SpringBootDistributionDiscoverer}.
 *
 * @author Mikhail Polivakha
 */
class SpringBootDistributionDiscovererTest {

    private SpringBootDistributionDiscoverer discoverer;

    @BeforeEach
    void setUp() {
        discoverer = new SpringBootDistributionDiscoverer();
    }

    /**
     * Currently, it is not going to be possible to not discover spring boot in
     * the service, but later in the future it maybe will be possible when we introduce the
     * support for quarkus.
     */
    @Test
    void testDiscoverNoSpringBoot() {

        // when.
        SoftwareDistribution result = discoverer.discover(createBuildInfo(
                new JarClassPathEntry("org.hibernate", "hibernate-core", "6.1.7", null, null),
                new JarClassPathEntry("org.jboss", "jandex", "2.4.2", null, null),
                new JarClassPathEntry("org.jetbrains.kotlin", "kotlin-stdlib", "2.0.17", null, null)));

        // then.
        assertThat(result).isNull();
    }

    @Test
    void testDiscoverSpringBootHappyPath() {

        // when.
        String springBootVersion = "2.4.2";

        SoftwareDistribution result = discoverer.discover(createBuildInfo(
                new JarClassPathEntry("org.hibernate", "hibernate-core", "6.1.7", null, null),
                new JarClassPathEntry("org.jboss", "jandex", "2.0.6", null, null),
                new JarClassPathEntry("org.springframework.boot", "spring-boot", springBootVersion, null, null),
                new JarClassPathEntry("org.jetbrains.kotlin", "kotlin-stdlib", "2.0.17", null, null)));

        // then.
        assertThat(result).isNotNull();
        assertThat(result.version()).isEqualTo(springBootVersion);
    }
}
