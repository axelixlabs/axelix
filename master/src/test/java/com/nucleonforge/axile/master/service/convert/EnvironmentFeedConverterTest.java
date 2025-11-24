package com.nucleonforge.axile.master.service.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.env.AxilePropertyValue;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse;
import com.nucleonforge.axile.master.service.convert.response.environment.EnvironmentFeedConverter;

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

        EnvironmentFeedResponse.PropertySourceShortProfile propertySource1 =
                getPropertySourceProfileByName(environmentFeedResponse, "systemProperties");
        assertThat(propertySource1.properties())
                .hasSize(2)
                .anySatisfy(kv -> {
                    assertThat(kv.name()).isEqualTo("java.vm.vendor");
                    assertThat(kv.value()).isEqualTo("Amazon.com Inc.");
                    assertThat(kv.isPrimary()).isTrue();
                    assertThat(kv.configPropsBeanName())
                            .isEqualTo("org.springframework.boot.test.property.SystemProperties");
                })
                .anySatisfy(kv -> {
                    assertThat(kv.name()).isEqualTo("org.jboss.logging.provider");
                    assertThat(kv.value()).isEqualTo("slf4j");
                    assertThat(kv.isPrimary()).isFalse();
                    assertThat(kv.configPropsBeanName()).isNull();
                });

        EnvironmentFeedResponse.PropertySourceShortProfile propertySource2 =
                getPropertySourceProfileByName(environmentFeedResponse, "systemEnvironment");
        assertThat(propertySource2.properties()).hasSize(1).anySatisfy(kv -> {
            assertThat(kv.name()).isEqualTo("JAVA_HOME");
            assertThat(kv.value()).isEqualTo(".jdks\\corretto-17.0.16");
            assertThat(kv.isPrimary()).isTrue();
            assertThat(kv.configPropsBeanName()).isEqualTo("org.springframework.boot.test.property.SystemEnvironment");
        });

        EnvironmentFeedResponse.PropertySourceShortProfile propertySource3 = getPropertySourceProfileByName(
                environmentFeedResponse, "Config resource class path resource [application.yaml]");
        assertThat(propertySource3.properties())
                .hasSize(2)
                .anySatisfy(kv -> {
                    assertThat(kv.name()).isEqualTo("spring.datasource.driver-class-sourceName");
                    assertThat(kv.value()).isEqualTo("org.h2.Driver");
                    assertThat(kv.isPrimary()).isFalse();
                    assertThat(kv.configPropsBeanName()).isNull();
                })
                .anySatisfy(kv -> {
                    assertThat(kv.name()).isEqualTo("spring.jpa.hibernate.ddl-auto");
                    assertThat(kv.value()).isEqualTo("create-drop");
                    assertThat(kv.isPrimary()).isFalse();
                    assertThat(kv.configPropsBeanName()).isNull();
                });

        EnvironmentFeedResponse.PropertySourceShortProfile propertySource4 =
                getPropertySourceProfileByName(environmentFeedResponse, "springCloudClientHostInfo");
        assertThat(propertySource4.properties())
                .hasSize(2)
                .anySatisfy(kv -> {
                    assertThat(kv.name()).isEqualTo("spring.cloud.client.hostname");
                    assertThat(kv.value()).isEqualTo("DESKTOP-111");
                    assertThat(kv.isPrimary()).isFalse();
                    assertThat(kv.configPropsBeanName())
                            .isEqualTo(
                                    "org.springframework.cloud.spring.cloud.client.hostname.SpringCloudClientHostInfo");
                })
                .anySatisfy(kv -> {
                    assertThat(kv.name()).isEqualTo("spring.cloud.client.ip-address");
                    assertThat(kv.value()).isEqualTo("192.0.0.0");
                    assertThat(kv.isPrimary()).isFalse();
                    assertThat(kv.configPropsBeanName())
                            .isEqualTo(
                                    "org.springframework.cloud.spring.cloud.client.hostname.SpringCloudClientHostInfo");
                });
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
                        new AxilePropertyValue(
                                "Amazon.com Inc.",
                                null,
                                true,
                                "org.springframework.boot.test.property.SystemProperties"),
                        "org.jboss.logging.provider",
                        new AxilePropertyValue("slf4j", null, false, null)));

        EnvironmentFeed.PropertySource propertySource2 = new EnvironmentFeed.PropertySource(
                "systemEnvironment",
                Map.of(
                        "JAVA_HOME",
                        new AxilePropertyValue(
                                ".jdks\\corretto-17.0.16",
                                "System Environment Property \"JAVA_HOME\"",
                                true,
                                "org.springframework.boot.test.property.SystemEnvironment")));

        EnvironmentFeed.PropertySource propertySource3 = new EnvironmentFeed.PropertySource(
                "Config resource class path resource [application.yaml]",
                Map.of(
                        "spring.datasource.driver-class-sourceName",
                        new AxilePropertyValue(
                                "org.h2.Driver", "class path resource [application.yaml] - 4:24", false, null),
                        "spring.jpa.hibernate.ddl-auto",
                        new AxilePropertyValue(
                                "create-drop", "class path resource [application.yaml] - 9:17", false, null)));

        EnvironmentFeed.PropertySource propertySource4 = new EnvironmentFeed.PropertySource(
                "springCloudClientHostInfo",
                Map.of(
                        "spring.cloud.client.hostname",
                        new AxilePropertyValue(
                                "DESKTOP-111",
                                null,
                                false,
                                "org.springframework.cloud.spring.cloud.client.hostname.SpringCloudClientHostInfo"),
                        "spring.cloud.client.ip-address",
                        new AxilePropertyValue(
                                "192.0.0.0",
                                null,
                                false,
                                "org.springframework.cloud.spring.cloud.client.hostname.SpringCloudClientHostInfo")));

        return new ArrayList<>(List.of(propertySource1, propertySource2, propertySource3, propertySource4));
    }
}
