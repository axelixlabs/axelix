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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.SpringVersion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.details.DefaultServiceDetailsAssemblerTest.DefaultServiceDetailsAssemblerTestConfig;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

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
@Import({DefaultServiceDetailsAssemblerTestConfig.class, AxelixDetailsEndpoint.class, JwtAuthTestConfiguration.class})
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

        assertThatJson(responseBody).node("git").isNotNull();
        assertThatJson(responseBody)
                .inPath("git")
                .isObject()
                .contains(
                        entry("commitShaShort", "a8b0929"),
                        entry("branch", "main"),
                        entry("commitTimestamp", "1761249922000"))
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
                        entry("springCloudVersion", "2022.0.4"));

        assertThatJson(responseBody)
                .inPath("runtime")
                .isObject()
                .containsKeys("javaVersion", "jdkVendor", "garbageCollector");

        assertThatJson(responseBody).node("build").isNotNull();
        assertThatJson(responseBody)
                .inPath("build")
                .isObject()
                .containsOnly(
                        entry("artifact", "axelix-sbs"),
                        entry("version", "1.0.0-SNAPSHOT"),
                        entry("group", "com.axelixlabs.axelix"),
                        entry("time", "2025-10-30T09:10:13.428Z"));

        assertThatJson(responseBody).inPath("os").isObject().containsOnlyKeys("name", "version", "arch");
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-details")
    void negativeAuthTests() {}
}
