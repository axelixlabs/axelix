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
package com.nucleonforge.axelix.sbs.spring.env;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.StandardEnvironment;

import com.nucleonforge.axelix.common.api.env.EnvironmentFeed;
import com.nucleonforge.axelix.common.api.env.EnvironmentFeed.Property;
import com.nucleonforge.axelix.common.api.env.EnvironmentFeed.PropertySource;
import com.nucleonforge.axelix.sbs.spring.configprops.ConfigPropsConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultEnvPropertyEnricher}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(args = "--fooBar=fromArgs")
@EnableConfigurationProperties(ConfigPropsConfigurationProperties.class)
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
