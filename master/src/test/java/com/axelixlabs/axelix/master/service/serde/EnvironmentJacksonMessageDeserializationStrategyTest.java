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
        assertThat(environmentFeed.getActiveProfiles()).hasSize(1).containsOnly("production");
        assertThat(environmentFeed.getDefaultProfiles()).hasSize(2).containsOnly("default", "test");
        assertThat(environmentFeed.getPropertySources()).hasSize(4);

        PropertySource servletParams = environmentFeed.getPropertySources().stream()
                .filter(ps -> ps.getSourceName().equals("servletContextInitParams"))
                .findFirst()
                .orElseThrow();
        assertThat(servletParams.getProperties()).isEmpty();
        assertThat(servletParams.getSourceDescription())
                .isEqualTo(
                        "Contains the initialization parameters of the 'ServletContext', defined in 'web.xml' or set via 'ServletContext.setInitParameter()', and has higher priority than properties in 'jndiProperties' and 'StandardEnvironment'");

        PropertySource systemProps = environmentFeed.getPropertySources().stream()
                .filter(ps -> ps.getSourceName().equals("systemProperties"))
                .findFirst()
                .orElseThrow();
        assertThat(systemProps.getProperties()).hasSize(2);
        assertThat(systemProps.getSourceDescription())
                .isEqualTo(
                        "Contains all Java system properties (those set via -Dkey=value at JVM startup, as well as properties set via 'System.setProperty()' at runtime) and has higher priority than properties in 'systemEnvironment'");

        Property javaSpecVersion = systemProps.getProperties().stream()
                .filter(pv -> pv.getPropertyName().equals("java.specification.version"))
                .findFirst()
                .orElseThrow();
        assertThat(javaSpecVersion.getValue()).isEqualTo("17");
        assertThat(javaSpecVersion.isPrimary()).isTrue();
        assertThat(javaSpecVersion.getConfigPropsBeanName())
                .isEqualTo("org.springframework.boot.test.property.SystemProperties");
        assertThat(javaSpecVersion.getDescription()).isNull();
        assertThat(javaSpecVersion.getDeprecation()).isNull();
        assertThat(javaSpecVersion.getInjectionPoints()).isNull();

        Property javaVmVendor = systemProps.getProperties().stream()
                .filter(pv -> pv.getPropertyName().equals("java.vm.vendor"))
                .findFirst()
                .orElseThrow();
        assertThat(javaVmVendor.getValue()).isEqualTo("BellSoft");
        assertThat(javaVmVendor.isPrimary()).isTrue();
        assertThat(javaVmVendor.getConfigPropsBeanName())
                .isEqualTo("org.springframework.boot.test.property.SystemProperties");
        assertThat(javaVmVendor.getDescription()).isNull();
        assertThat(javaVmVendor.getDeprecation()).isNull();
        assertThat(javaVmVendor.getInjectionPoints()).isNotNull().hasSize(2);

        List<InjectionPoint> injectionPoints = javaVmVendor.getInjectionPoints();
        assertThat(injectionPoints.get(0))
                .extracting(
                        InjectionPoint::getBeanName,
                        InjectionPoint::getInjectionType,
                        InjectionPoint::getTargetName,
                        InjectionPoint::getPropertyExpression)
                .containsExactly("systemPropertiesBean", InjectionType.FIELD, "vendorField", "${java.vm.vendor}");

        assertThat(injectionPoints.get(1))
                .extracting(
                        InjectionPoint::getBeanName,
                        InjectionPoint::getInjectionType,
                        InjectionPoint::getTargetName,
                        InjectionPoint::getPropertyExpression)
                .containsExactly(
                        "appConfig",
                        InjectionType.CONSTRUCTOR_PARAMETER,
                        "vendorParam",
                        "#{systemProperties['java.vm.vendor']}");

        PropertySource systemEnv = environmentFeed.getPropertySources().stream()
                .filter(ps -> ps.getSourceName().equals("systemEnvironment"))
                .findFirst()
                .orElseThrow();
        assertThat(systemEnv.getProperties()).hasSize(2);
        assertThat(systemEnv.getSourceDescription())
                .isEqualTo(
                        "Contains all OS environment variables available to the 'JVM' process and has higher priority than properties from 'application.*'");

        Property javaHome = systemEnv.getProperties().stream()
                .filter(pv -> pv.getPropertyName().equals("JAVA_HOME"))
                .findFirst()
                .orElseThrow();
        assertThat(javaHome.getValue()).isEqualTo("Java_Liberica_jdk/17.0.16-12/x64");
        assertThat(javaHome.isPrimary()).isTrue();
        assertThat(javaHome.getConfigPropsBeanName()).isNull();
        assertThat(javaHome.getDescription()).isEqualTo("System Environment Property \"JAVA_HOME\"");
        assertThat(javaHome.getDeprecation()).isNull();
        assertThat(javaHome.getInjectionPoints()).isNull();

        Property loggingPath = systemEnv.getProperties().stream()
                .filter(pv -> pv.getPropertyName().equals("logging.path"))
                .findFirst()
                .orElseThrow();
        assertThat(loggingPath.getValue()).isEqualTo("pattern");
        assertThat(loggingPath.isPrimary()).isTrue();
        assertThat(loggingPath.getConfigPropsBeanName()).isNull();
        assertThat(loggingPath.getDescription()).isEqualTo("Location of the log file. For instance, `/var/log`.");
        assertThat(loggingPath.getDeprecation()).isNotNull();
        assertThat(loggingPath.getDeprecation().getMessage())
                .isEqualTo("Deprecated in favor of logging.file.path property.");
        assertThat(loggingPath.getInjectionPoints()).isNull();

        PropertySource configProps = environmentFeed.getPropertySources().stream()
                .filter(ps -> ps.getSourceName().equals("Config resource classpath:actuate/env/"))
                .findFirst()
                .orElseThrow();
        assertThat(configProps.getProperties()).hasSize(1);
        assertThat(configProps.getSourceDescription())
                .isEqualTo(
                        "Contains properties from the 'application.*' configuration file loaded from the classpath (optional:classpath:/) and serves as one of the primary Spring Boot configuration sources.");

        Property cacheMaxSize = configProps.getProperties().stream()
                .filter(pv -> pv.getPropertyName().equals("com.example.cache.max-size"))
                .findFirst()
                .orElseThrow();
        assertThat(cacheMaxSize.getValue()).isEqualTo("1000");
        assertThat(cacheMaxSize.isPrimary()).isTrue();
        assertThat(cacheMaxSize.getConfigPropsBeanName()).isNull();
        assertThat(cacheMaxSize.getDescription()).isNull();
        assertThat(cacheMaxSize.getDeprecation()).isNull();
        assertThat(cacheMaxSize.getInjectionPoints()).isNull();
    }
}
