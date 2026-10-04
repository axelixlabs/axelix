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
package com.axelixlabs.axelix.sbs.spring.core.details;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.SpringVersion;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.details.AxelixDetailsEndpointTest.CurrentConfig;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoProperties;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoPropertiesLoader;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultLibraryInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.LibraryInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoPropertiesLoader.AXELIX_INFO_LOCATION;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

/**
 * Integration tests for {@link AxelixDetailsEndpoint}.
 *
 * @since 30.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"management.endpoints.web.exposure.include=axelix-details"})
@Import({JwtAuthTestConfiguration.class, CurrentConfig.class})
class AxelixDetailsEndpointTest {

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Test
    void shouldReturnValidDetailsStructure() {
        ResponseEntity<String> response =
                restTemplate.asViewer().getForEntity("/actuator/axelix-details", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThatJson(responseBody).isObject().containsOnlyKeys("git", "spring", "runtime", "build", "os");
        assertThatJson(responseBody)
                .inPath("git")
                .isObject()
                .containsOnlyKeys("commitShaShort", "branch", "commitAuthor", "commitTimestamp");
        assertThatJson(responseBody)
                .inPath("spring")
                .isObject()
                .containsOnlyKeys("springBootVersion", "springFrameworkVersion", "springCloudVersion");
        assertThatJson(responseBody)
                .inPath("runtime")
                .isObject()
                .containsOnlyKeys("javaVersion", "jdkVendor", "kotlinVersion");

        assertThatJson(responseBody).node("git").isNotNull();
        assertThatJson(responseBody)
                .inPath("git")
                .isObject()
                .contains(entry("commitShaShort", "a8b0929"), entry("branch", "main"))
                .containsKeys("commitAuthor", "commitTimestamp");

        assertThatJson(responseBody)
                .inPath("git.commitAuthor")
                .isObject()
                .contains(entry("name", "Mikhail Polivakha"), entry("email", "mikhailpolivakha@email.com"));

        assertThatJson(responseBody)
                .inPath("git.commitAuthor")
                .isObject()
                .containsOnly(entry("name", "Mikhail Polivakha"), entry("email", "mikhailpolivakha@email.com"));

        assertThatJson(responseBody)
                .inPath("spring")
                .isObject()
                .contains(
                        entry("springBootVersion", SpringBootVersion.getVersion()),
                        entry("springFrameworkVersion", SpringVersion.getVersion()),
                        entry("springCloudVersion", "2021.0.9"));

        assertThatJson(responseBody).inPath("runtime").isObject().containsKeys("javaVersion", "jdkVendor");

        assertThatJson(responseBody).node("build").isNotNull();
        assertThatJson(responseBody)
                .inPath("build")
                .isObject()
                .containsOnly(
                        entry("artifact", "axelix-sbs"),
                        entry("version", "1.0.0-SNAPSHOT"),
                        entry("group", "com.axelixlabs"),
                        entry("time", "2026-07-24T09:33:48.541842752Z"));

        assertThatJson(responseBody).inPath("os").isObject().containsOnlyKeys("name", "version", "arch");
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-details")
    void negativeAuthTests() {}

    @TestConfiguration
    static class CurrentConfig {

        @Bean
        public LibraryInformationProvider libraryInformationProvider() {
            return new DefaultLibraryInformationProvider();
        }

        @Bean
        public ServiceDetailsAssembler serviceDetailsAssembler(
                AxelixInfoProperties axelixInfoProperties, LibraryInformationProvider libraryInformationProvider) {
            return new DefaultServiceDetailsAssembler(axelixInfoProperties, libraryInformationProvider);
        }

        @Bean
        public AxelixInfoProperties axelixInfoProperties(@Value(AXELIX_INFO_LOCATION) Resource axelixInfoResource)
                throws IOException {

            try (InputStream inputStream = axelixInfoResource.getInputStream()) {
                return AxelixInfoPropertiesLoader.load(inputStream);
            }
        }

        @Bean
        public AxelixDetailsEndpoint axelixDetailsEndpoint(ServiceDetailsAssembler serviceDetailsAssembler) {
            return new AxelixDetailsEndpoint(serviceDetailsAssembler);
        }
    }
}
