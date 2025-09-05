package com.nucleonforge.axile.master.service.versions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.domain.JarClassPathEntry;
import com.nucleonforge.axile.master.model.software.SoftwareDistribution;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
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
        SoftwareDistribution result = discoverer.discover(createInstance(
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

        SoftwareDistribution result = discoverer.discover(createInstance(
                new JarClassPathEntry("org.hibernate", "hibernate-core", "6.1.7", null, null),
                new JarClassPathEntry("org.jboss", "jandex", "2.0.6", null, null),
                new JarClassPathEntry("org.springframework.boot", "spring-boot", springBootVersion, null, null),
                new JarClassPathEntry("org.jetbrains.kotlin", "kotlin-stdlib", "2.0.17", null, null)));

        // then.
        assertThat(result).isNotNull();
        assertThat(result.version()).isEqualTo(springBootVersion);
    }
}
