/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.serde;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionPoint;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionType;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.Property;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EnvironmentJacksonMessageDeserializationStrategy}. The json for deserialization was taken from
 * <a href="https://docs.spring.io/spring-boot/api/rest/actuator/env.html">official doc</a>,shortened,
 * and extended with additional active and default profiles for testing purposes.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
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
              "sourceName": "servletContextInitParams",
              "sourceDescription": "Contains the initialization parameters of the 'ServletContext', defined in 'web.xml' or set via 'ServletContext.setInitParameter()', and has higher priority than properties in 'jndiProperties' and 'StandardEnvironment'",
              "properties": []
            },
            {
              "sourceName": "systemProperties",
              "sourceDescription": "Contains all Java system properties (those set via -Dkey=value at JVM startup, as well as properties set via 'System.setProperty()' at runtime) and has higher priority than properties in 'systemEnvironment'",
              "properties": [
                {
                  "propertyName": "java.specification.version",
                  "value": "17",
                  "isPrimary": true,
                  "configPropsBeanName": "org.springframework.boot.test.property.SystemProperties",
                  "description": null,
                  "injectionPoints": null
                },
                {
                  "propertyName": "java.vm.vendor",
                  "value": "BellSoft",
                  "isPrimary": true,
                  "configPropsBeanName": "org.springframework.boot.test.property.SystemProperties",
                  "description": null,
                  "injectionPoints": [
                    {
                     "beanName": "systemPropertiesBean",
                      "injectionType": "FIELD",
                      "targetName": "vendorField",
                      "propertyExpression": "${java.vm.vendor}"
                    },
                    {
                      "beanName": "appConfig",
                      "injectionType": "CONSTRUCTOR_PARAMETER",
                      "targetName": "vendorParam",
                      "propertyExpression": "#{systemProperties['java.vm.vendor']}"
                    }
                  ]
                }
              ]
            },
            {
              "sourceName": "systemEnvironment",
              "sourceDescription": "Contains all OS environment variables available to the 'JVM' process and has higher priority than properties from 'application.*'",
              "properties": [
                {
                  "propertyName": "JAVA_HOME",
                  "value": "Java_Liberica_jdk/17.0.16-12/x64",
                  "isPrimary": true,
                  "configPropsBeanName": null,
                  "description": "System Environment Property \\"JAVA_HOME\\""
                },
                {
                  "propertyName": "logging.path",
                  "value": "pattern",
                  "isPrimary": true,
                  "configPropsBeanName": null,
                  "description": "Location of the log file. For instance, `/var/log`.",
                  "deprecation": {
                    "message": "Deprecated in favor of logging.file.path property."
                  }
                }
              ]
            },
            {
              "sourceName": "Config resource classpath:actuate/env/",
              "sourceDescription": "Contains properties from the 'application.*' configuration file loaded from the classpath (optional:classpath:/) and serves as one of the primary Spring Boot configuration sources.",
              "properties": [
                {
                  "propertyName": "com.example.cache.max-size",
                  "value": "1000",
                  "isPrimary": true,
                  "configPropsBeanName": null,
                  "description": null
                }
              ]
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

        PropertySource servletParams = environmentFeed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("servletContextInitParams"))
                .findFirst()
                .orElseThrow();
        assertThat(servletParams.properties()).isEmpty();
        assertThat(servletParams.sourceDescription())
                .isEqualTo(
                        "Contains the initialization parameters of the 'ServletContext', defined in 'web.xml' or set via 'ServletContext.setInitParameter()', and has higher priority than properties in 'jndiProperties' and 'StandardEnvironment'");

        PropertySource systemProps = environmentFeed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("systemProperties"))
                .findFirst()
                .orElseThrow();
        assertThat(systemProps.properties()).hasSize(2);
        assertThat(systemProps.sourceDescription())
                .isEqualTo(
                        "Contains all Java system properties (those set via -Dkey=value at JVM startup, as well as properties set via 'System.setProperty()' at runtime) and has higher priority than properties in 'systemEnvironment'");

        Property javaSpecVersion = systemProps.properties().stream()
                .filter(pv -> pv.propertyName().equals("java.specification.version"))
                .findFirst()
                .orElseThrow();
        assertThat(javaSpecVersion.value()).isEqualTo("17");
        assertThat(javaSpecVersion.isPrimary()).isTrue();
        assertThat(javaSpecVersion.configPropsBeanName())
                .isEqualTo("org.springframework.boot.test.property.SystemProperties");
        assertThat(javaSpecVersion.description()).isNull();
        assertThat(javaSpecVersion.deprecation()).isNull();
        assertThat(javaSpecVersion.injectionPoints()).isNull();

        Property javaVmVendor = systemProps.properties().stream()
                .filter(pv -> pv.propertyName().equals("java.vm.vendor"))
                .findFirst()
                .orElseThrow();
        assertThat(javaVmVendor.value()).isEqualTo("BellSoft");
        assertThat(javaVmVendor.isPrimary()).isTrue();
        assertThat(javaVmVendor.configPropsBeanName())
                .isEqualTo("org.springframework.boot.test.property.SystemProperties");
        assertThat(javaVmVendor.description()).isNull();
        assertThat(javaVmVendor.deprecation()).isNull();
        assertThat(javaVmVendor.injectionPoints()).isNotNull().hasSize(2);

        List<InjectionPoint> injectionPoints = javaVmVendor.injectionPoints();
        assertThat(injectionPoints.get(0))
                .extracting(
                        InjectionPoint::beanName,
                        InjectionPoint::injectionType,
                        InjectionPoint::targetName,
                        InjectionPoint::propertyExpression)
                .containsExactly("systemPropertiesBean", InjectionType.FIELD, "vendorField", "${java.vm.vendor}");

        assertThat(injectionPoints.get(1))
                .extracting(
                        InjectionPoint::beanName,
                        InjectionPoint::injectionType,
                        InjectionPoint::targetName,
                        InjectionPoint::propertyExpression)
                .containsExactly(
                        "appConfig",
                        InjectionType.CONSTRUCTOR_PARAMETER,
                        "vendorParam",
                        "#{systemProperties['java.vm.vendor']}");

        PropertySource systemEnv = environmentFeed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("systemEnvironment"))
                .findFirst()
                .orElseThrow();
        assertThat(systemEnv.properties()).hasSize(2);
        assertThat(systemEnv.sourceDescription())
                .isEqualTo(
                        "Contains all OS environment variables available to the 'JVM' process and has higher priority than properties from 'application.*'");

        Property javaHome = systemEnv.properties().stream()
                .filter(pv -> pv.propertyName().equals("JAVA_HOME"))
                .findFirst()
                .orElseThrow();
        assertThat(javaHome.value()).isEqualTo("Java_Liberica_jdk/17.0.16-12/x64");
        assertThat(javaHome.isPrimary()).isTrue();
        assertThat(javaHome.configPropsBeanName()).isNull();
        assertThat(javaHome.description()).isEqualTo("System Environment Property \"JAVA_HOME\"");
        assertThat(javaHome.deprecation()).isNull();
        assertThat(javaHome.injectionPoints()).isNull();

        Property loggingPath = systemEnv.properties().stream()
                .filter(pv -> pv.propertyName().equals("logging.path"))
                .findFirst()
                .orElseThrow();
        assertThat(loggingPath.value()).isEqualTo("pattern");
        assertThat(loggingPath.isPrimary()).isTrue();
        assertThat(loggingPath.configPropsBeanName()).isNull();
        assertThat(loggingPath.description()).isEqualTo("Location of the log file. For instance, `/var/log`.");
        assertThat(loggingPath.deprecation()).isNotNull();
        assertThat(loggingPath.deprecation().message()).isEqualTo("Deprecated in favor of logging.file.path property.");
        assertThat(loggingPath.injectionPoints()).isNull();

        PropertySource configProps = environmentFeed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("Config resource classpath:actuate/env/"))
                .findFirst()
                .orElseThrow();
        assertThat(configProps.properties()).hasSize(1);
        assertThat(configProps.sourceDescription())
                .isEqualTo(
                        "Contains properties from the 'application.*' configuration file loaded from the classpath (optional:classpath:/) and serves as one of the primary Spring Boot configuration sources.");

        Property cacheMaxSize = configProps.properties().stream()
                .filter(pv -> pv.propertyName().equals("com.example.cache.max-size"))
                .findFirst()
                .orElseThrow();
        assertThat(cacheMaxSize.value()).isEqualTo("1000");
        assertThat(cacheMaxSize.isPrimary()).isTrue();
        assertThat(cacheMaxSize.configPropsBeanName()).isNull();
        assertThat(cacheMaxSize.description()).isNull();
        assertThat(cacheMaxSize.deprecation()).isNull();
        assertThat(cacheMaxSize.injectionPoints()).isNull();
    }
}
