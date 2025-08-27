package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EnvironmentJacksonMessageDeserializationStrategy}. The json for deserialization was taken from
 * <a href="https://docs.spring.io/spring-boot/api/rest/actuator/env.html">official doc</a>,shortened,
 * and extended with additional active and default profiles for testing purposes.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 */
class EnvironmentJacksonMessageDeserializationStrategyTest {

    private final EnvironmentJacksonMessageDeserializationStrategy subject =
            new EnvironmentJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeEnvironmentFeed() {
        // language=json
        String response =
                """
            {
              "activeProfiles": ["production"],
              "defaultProfiles": ["default", "test"],
              "propertySources": [
                {
                  "name": "servletContextInitParams",
                  "properties": {}
                },
                {
                  "name": "systemProperties",
                  "properties": {
                    "java.specification.version": {
                      "value": "17"
                    },
                    "java.class.path": {
                      "value": "gradle-worker.jar"
                    },
                    "java.vm.vendor": {
                      "value": "BellSoft"
                    }
                  }
                },
                {
                  "name": "systemEnvironment",
                  "properties": {
                    "JAVA_HOME": {
                      "value": "/opt/Java_Liberica_jdk/17.0.16-12/x64",
                      "origin": "System Environment Property \\"JAVA_HOME\\""
                    }
                  }
                },
                {
                  "name": "Config resource classpath:actuate/env/",
                  "properties": {
                    "com.example.cache.max-size": {
                      "value": "1000",
                      "origin": "class path resource [/env/application.properties]"
                    }
                  }
                }
              ]
            }
            """;

        // when.
        EnvironmentFeed environmentFeed = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // then.
        assertThat(environmentFeed.activeProfiles()).hasSize(1).containsOnly("production");
        assertThat(environmentFeed.defaultProfiles()).hasSize(2).containsOnly("default", "test");
        assertThat(environmentFeed.propertySources()).hasSize(4);

        EnvironmentFeed.PropertySource servletParams = environmentFeed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("servletContextInitParams"))
                .findFirst()
                .orElseThrow();

        assertThat(servletParams.properties()).isEmpty();

        EnvironmentFeed.PropertySource systemProps = environmentFeed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("systemProperties"))
                .findFirst()
                .orElseThrow();

        assertThat(systemProps.properties())
                .hasSize(3)
                .extractingByKey("java.specification.version")
                .satisfies(pv -> assertThat(pv.value()).isEqualTo("17"));

        assertThat(systemProps.properties()).extractingByKey("java.class.path").satisfies(pv -> assertThat(pv.value())
                .isEqualTo("gradle-worker.jar"));

        assertThat(systemProps.properties()).extractingByKey("java.vm.vendor").satisfies(pv -> assertThat(pv.value())
                .isEqualTo("BellSoft"));

        EnvironmentFeed.PropertySource systemEnv = environmentFeed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("systemEnvironment"))
                .findFirst()
                .orElseThrow();

        assertThat(systemEnv.properties())
                .hasSize(1)
                .extractingByKey("JAVA_HOME")
                .satisfies(pv -> {
                    assertThat(pv.value()).isEqualTo("/opt/Java_Liberica_jdk/17.0.16-12/x64");
                    assertThat(pv.origin()).isEqualTo("System Environment Property \"JAVA_HOME\"");
                });
    }
}
