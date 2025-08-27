package com.nucleonforge.axile.master.service.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.PropertyValue;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EnvironmentFeedConverter}
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 */
class EnvironmentFeedConverterTest {

    private final EnvironmentFeedConverter subject = new EnvironmentFeedConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        EnvironmentFeedResponse environmentFeedResponse = subject.convertInternal(getEnvironmentFeed());

        // then.
        assertThat(environmentFeedResponse)
                .extracting(EnvironmentFeedResponse::activeProfiles)
                .satisfies(activeProfiles -> {
                    assertThat(activeProfiles).hasSize(1).containsOnly("production");
                });

        assertThat(environmentFeedResponse)
                .extracting(EnvironmentFeedResponse::defaultProfiles)
                .satisfies(defaultProfiles ->
                        assertThat(defaultProfiles).hasSize(2).containsOnly("default", "development"));

        EnvironmentFeedResponse.PropertySourceShortProfile propertySourceShortProfile1 =
                getPropertySourceProfileByName(environmentFeedResponse, "systemProperties");
        assertThat(propertySourceShortProfile1.properties())
                .hasSize(2)
                .containsEntry("java.vm.vendor", "Amazon.com Inc.")
                .containsEntry("org.jboss.logging.provider", "slf4j");

        EnvironmentFeedResponse.PropertySourceShortProfile propertySourceShortProfile2 =
                getPropertySourceProfileByName(environmentFeedResponse, "systemEnvironment");
        assertThat(propertySourceShortProfile2.properties())
                .hasSize(1)
                .containsEntry("JAVA_HOME", ".jdks\\corretto-17.0.16");

        EnvironmentFeedResponse.PropertySourceShortProfile propertySourceShortProfile3 = getPropertySourceProfileByName(
                environmentFeedResponse, "Config resource class path resource [application.yaml]");
        assertThat(propertySourceShortProfile3.properties())
                .hasSize(2)
                .containsEntry("spring.datasource.driver-class-sourceName", "org.h2.Driver")
                .containsEntry("spring.jpa.hibernate.ddl-auto", "create-drop");

        EnvironmentFeedResponse.PropertySourceShortProfile propertySourceShortProfile4 =
                getPropertySourceProfileByName(environmentFeedResponse, "springCloudClientHostInfo");
        assertThat(propertySourceShortProfile4.properties())
                .hasSize(2)
                .containsEntry("spring.cloud.client.hostname", "DESKTOP-111")
                .containsEntry("spring.cloud.client.ip-address", "192.0.0.0");
    }

    private static EnvironmentFeedResponse.PropertySourceShortProfile getPropertySourceProfileByName(
            EnvironmentFeedResponse environmentFeedResponse, String propertyName) {
        return environmentFeedResponse.propertySources().stream()
                .filter(property -> property.name().equals(propertyName))
                .findFirst()
                .orElseThrow();
    }

    private static EnvironmentFeed getEnvironmentFeed() {
        List<String> activeProfiles = new ArrayList<>();
        activeProfiles.add("production");

        List<String> defaultProfiles = new ArrayList<>();
        defaultProfiles.add("default");
        defaultProfiles.add("development");

        return new EnvironmentFeed(activeProfiles, defaultProfiles, getPropertySources());
    }

    private static List<EnvironmentFeed.PropertySource> getPropertySources() {

        EnvironmentFeed.PropertySource propertySource1 = new EnvironmentFeed.PropertySource(
                "systemProperties",
                Map.of(
                        "java.vm.vendor",
                        new PropertyValue("Amazon.com Inc.", null),
                        "org.jboss.logging.provider",
                        new PropertyValue("slf4j", null)));

        EnvironmentFeed.PropertySource propertySource2 = new EnvironmentFeed.PropertySource(
                "systemEnvironment",
                Map.of(
                        "JAVA_HOME",
                        new PropertyValue(".jdks\\corretto-17.0.16", "System Environment Property \"JAVA_HOME\"")));

        EnvironmentFeed.PropertySource propertySource3 = new EnvironmentFeed.PropertySource(
                "Config resource class path resource [application.yaml]",
                Map.of(
                        "spring.datasource.driver-class-sourceName",
                        new PropertyValue("org.h2.Driver", "class path resource [application.yaml] - 4:24"),
                        "spring.jpa.hibernate.ddl-auto",
                        new PropertyValue("create-drop", "class path resource [application.yaml] - 9:17")));

        EnvironmentFeed.PropertySource propertySource4 = new EnvironmentFeed.PropertySource(
                "springCloudClientHostInfo",
                Map.of(
                        "spring.cloud.client.hostname", new PropertyValue("DESKTOP-111", null),
                        "spring.cloud.client.ip-address", new PropertyValue("192.0.0.0", null)));

        return new ArrayList<>(List.of(propertySource1, propertySource2, propertySource3, propertySource4));
    }
}
