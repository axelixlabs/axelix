package com.nucleonforge.axile.sbs.spring.env;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.StandardEnvironment;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.Property;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultEnvPropertyEnricher}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(args = "--fooBar=fromArgs")
@Import(EnvironmentTestConfig.class)
class DefaultEnvPropertyEnricherTest {

    @Autowired
    private EnvironmentEndpoint environmentEndpoint;

    @Autowired
    private EnvPropertyEnricher enricher;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("foo.bar", "system.property");
    }

    @Test
    void shouldEnrichAllPropertiesWithPrimaryField() {
        EnvironmentDescriptor defaultDescriptor = environmentEndpoint.environment(null);

        EnvironmentFeed environmentFeed = enricher.enrich(defaultDescriptor);

        assertThat(environmentFeed).isNotNull();
        assertThat(environmentFeed.activeProfiles()).isNotNull();
        assertThat(environmentFeed.defaultProfiles()).isNotNull();
        assertThat(environmentFeed.propertySources()).isNotEmpty();

        // property from the command line args should win
        // https://docs.spring.io/spring-boot/reference/features/external-config.html
        assertThat(findProperty(environmentFeed, StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, "foo.bar")
                        .isPrimary())
                .isFalse();

        assertThat(findProperty(environmentFeed, "commandLineArgs", "fooBar").isPrimary())
                .isTrue();
    }

    private static Property findProperty(
            EnvironmentFeed environmentFeed, String propertySourceName, String propertyName) {
        PropertySource propertySource = environmentFeed.propertySources().stream()
                .filter(it -> it.sourceName().equals(propertySourceName))
                .findFirst()
                .orElseThrow();

        return propertySource.properties().stream()
                .filter(it -> it.propertyName().equals(propertyName))
                .findFirst()
                .orElseThrow();
    }
}
