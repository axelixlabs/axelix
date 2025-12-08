/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.service.convert;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.Property;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.PropertySource;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse.PropertySourceShortProfile;
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

        PropertySourceShortProfile propertySource1 =
                getPropertySourceProfileByName(environmentFeedResponse, "systemProperties");
        assertThat(propertySource1.properties()).hasSize(1).anySatisfy(kv -> {
            assertThat(kv.name()).isEqualTo("java.vm.vendor");
            assertThat(kv.value()).isEqualTo("Amazon.com Inc.");
            assertThat(kv.isPrimary()).isTrue();
            assertThat(kv.configPropsBeanName()).isEqualTo("org.springframework.boot.test.property.SystemProperties");
            assertThat(kv.description()).isNull();
            assertThat(kv.deprecation()).isNull();
        });

        PropertySourceShortProfile propertySource2 =
                getPropertySourceProfileByName(environmentFeedResponse, "systemEnvironment");
        assertThat(propertySource2.properties()).hasSize(1).anySatisfy(kv -> {
            assertThat(kv.name()).isEqualTo("JAVA_HOME");
            assertThat(kv.value()).isEqualTo(".jdks\\corretto-17.0.16");
            assertThat(kv.isPrimary()).isTrue();
            assertThat(kv.configPropsBeanName()).isEqualTo("org.springframework.boot.test.property.SystemEnvironment");
            assertThat(kv.description()).isNull();
            assertThat(kv.deprecation()).isNull();
        });

        PropertySourceShortProfile propertySource3 = getPropertySourceProfileByName(
                environmentFeedResponse, "Config resource class path resource [application.yaml]");
        assertThat(propertySource3.properties())
                .hasSize(2)
                .anySatisfy(kv -> {
                    assertThat(kv.name()).isEqualTo("spring.datasource.driver-class-sourceName");
                    assertThat(kv.value()).isEqualTo("org.h2.Driver");
                    assertThat(kv.isPrimary()).isFalse();
                    assertThat(kv.configPropsBeanName()).isNull();
                    assertThat(kv.description()).isNull();
                    assertThat(kv.deprecation()).isNull();
                })
                .anySatisfy(kv -> {
                    assertThat(kv.name()).isEqualTo("spring.jpa.hibernate.ddl-auto");
                    assertThat(kv.value()).isEqualTo("create-drop");
                    assertThat(kv.isPrimary()).isFalse();
                    assertThat(kv.configPropsBeanName()).isNull();
                    assertThat(kv.description())
                            .isEqualTo(
                                    "DDL mode. This is actually a shortcut for the \"hibernate.hbm2ddl.auto\" property.");
                    assertThat(kv.deprecation()).isNull();
                });

        PropertySourceShortProfile propertySource4 =
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
                    assertThat(kv.description()).isNull();
                    assertThat(kv.deprecation()).isNull();
                })
                .anySatisfy(kv -> {
                    assertThat(kv.name()).isEqualTo("logging.path");
                    assertThat(kv.value()).isEqualTo("pattern");
                    assertThat(kv.isPrimary()).isFalse();
                    assertThat(kv.configPropsBeanName()).isNull();
                    assertThat(kv.description()).isEqualTo("Location of the log file. For instance, `/var/log`.");
                    assertThat(kv.deprecation()).isNull();
                });
    }

    private static PropertySourceShortProfile getPropertySourceProfileByName(
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

    private static List<PropertySource> getPropertySources() {
        List<Property> properties1 = new ArrayList<>();
        properties1.add(new Property(
                "java.vm.vendor",
                "Amazon.com Inc.",
                true,
                "org.springframework.boot.test.property.SystemProperties",
                null,
                null));
        PropertySource propertySource1 = new PropertySource("systemProperties", properties1);

        List<Property> properties2 = new ArrayList<>();
        properties2.add(new Property(
                "JAVA_HOME",
                ".jdks\\corretto-17.0.16",
                true,
                "org.springframework.boot.test.property.SystemEnvironment",
                null,
                null));
        PropertySource propertySource2 = new PropertySource("systemEnvironment", properties2);

        List<Property> properties3 = new ArrayList<>();
        properties3.add(
                new Property("spring.datasource.driver-class-sourceName", "org.h2.Driver", false, null, null, null));

        properties3.add(new Property(
                "spring.jpa.hibernate.ddl-auto",
                "create-drop",
                false,
                null,
                "DDL mode. This is actually a shortcut for the \"hibernate.hbm2ddl.auto\" property.",
                null));
        PropertySource propertySource3 =
                new PropertySource("Config resource class path resource [application.yaml]", properties3);

        List<Property> properties4 = new ArrayList<>();
        properties4.add(new Property(
                "spring.cloud.client.hostname",
                "DESKTOP-111",
                false,
                "org.springframework.cloud.spring.cloud.client.hostname.SpringCloudClientHostInfo",
                null,
                null));

        properties4.add(new Property(
                "logging.path", "pattern", false, null, "Location of the log file. For instance, `/var/log`.", null));
        PropertySource propertySource4 = new PropertySource("springCloudClientHostInfo", properties4);

        return new ArrayList<>(List.of(propertySource1, propertySource2, propertySource3, propertySource4));
    }
}
