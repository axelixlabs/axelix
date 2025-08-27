package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.env.EnvironmentProperty;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EnvironmentPropertyJacksonMessageDeserializationStrategyTest}.
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
class EnvironmentPropertyJacksonMessageDeserializationStrategyTest {

    private final EnvironmentPropertyJacksonMessageDeserializationStrategy subject =
            new EnvironmentPropertyJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeEnvironmentProperty() {
        // language=json
        String response =
                """
            {
              "property": {
                "source":"systemProperties",
                "value":"Java Virtual Machine Specification"
              },
              "activeProfiles":["production", "test"],
              "defaultProfiles":["default"],
              "propertySources":[
                {
                  "name":"servletContextInitParams"
                },
                {
                  "name":"systemEnvironment"
                },
                {
                  "name":"systemProperties",
                  "property": {
                    "value":"Java Virtual Machine Specification"
                  }
                },
                {
                  "name": "cachedrandom"
                },
                {
                  "name": "springCloudClientHostInfo"
                },
                {
                  "name": "Config resource 'class path resource [application.yaml]' via location 'optional:classpath:/'"
                },
                {
                 "name": "Management Server"
                }
               ]
              }
            """;

        // when.
        EnvironmentProperty environmentProperty = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // then.
        assertThat(environmentProperty.property().source()).isEqualTo("systemProperties");
        assertThat(environmentProperty.property().value()).isEqualTo("Java Virtual Machine Specification");

        List<EnvironmentProperty.SourceEntry> sources = environmentProperty.propertySources();
        assertThat(sources).hasSize(7);

        assertThat(sources)
                .extracting(EnvironmentProperty.SourceEntry::sourceName)
                .containsExactly(
                        "servletContextInitParams",
                        "systemEnvironment",
                        "systemProperties",
                        "cachedrandom",
                        "springCloudClientHostInfo",
                        "Config resource 'class path resource [application.yaml]' via location 'optional:classpath:/'",
                        "Management Server");

        EnvironmentProperty.SourceEntry configSource = sources.stream()
                .filter(s -> s.sourceName().equals("systemProperties"))
                .findFirst()
                .orElseThrow();

        assertThat(configSource.property()).isNotNull();
        assertThat(configSource.property().value()).isEqualTo("Java Virtual Machine Specification");
        assertThat(configSource.property().origin()).isNull();

        sources.stream().filter(s -> !s.sourceName().equals("systemProperties")).forEach(s -> assertThat(s.property())
                .isNull());
    }
}
