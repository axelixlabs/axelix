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
package com.axelixlabs.axelix.sbs.spring.details;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.InstanceDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.BuildDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.GitDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.OsDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.RuntimeDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.SpringDetails;

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
@Import({
    DefaultServiceDetailsAssemblerTest.DefaultServiceDetailsAssemblerTestConfig.class,
    InstanceDetailsEndpointTest.AxelixDetailsEndpointTestConfig.class
})
class InstanceDetailsEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnValidDetailsStructure() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axelix-details", String.class);

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
                .contains(entry("springBootVersion", "3.5.0"), entry("springFrameworkVersion", "6.2.7"));

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

    @Test
    void shouldContainValidDetails() {
        ResponseEntity<InstanceDetails> response =
                restTemplate.getForEntity("/actuator/axelix-details", InstanceDetails.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        InstanceDetails details = response.getBody();
        assertThat(details).isNotNull();

        GitDetails git = details.git();
        assertThat(git).isNotNull();
        assertThat(git.commitShaShort()).isEqualTo("a8b0929");
        assertThat(git.branch()).isEqualTo("main");
        assertThat(git.commitAuthor().name()).isEqualTo("Mikhail Polivakha");
        assertThat(git.commitAuthor().email()).isEqualTo("mikhailpolivakha@email.com");
        assertThat(git.commitTimestamp()).isNotNull();

        SpringDetails spring = details.spring();
        assertThat(spring.springBootVersion()).isEqualTo("3.5.0");
        assertThat(spring.springFrameworkVersion()).isEqualTo("6.2.7");
        assertThat(spring.springCloudVersion()).isNull();

        RuntimeDetails runtime = details.runtime();
        assertThat(runtime).isNotNull();
        assertThat(runtime.javaVersion()).isNotBlank();
        assertThat(runtime.jdkVendor()).isNotBlank();
        assertThat(runtime.garbageCollector()).isNotBlank();
        assertThat(runtime.kotlinVersion()).isNull();

        BuildDetails build = details.build();
        assertThat(build).isNotNull();
        assertThat(build.artifact()).isEqualTo("axelix-sbs");
        assertThat(build.version()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(build.group()).isEqualTo("com.axelixlabs.axelix");
        assertThat(build.time()).isEqualTo("2025-10-30T09:10:13.428Z");

        OsDetails os = details.os();
        assertThat(os).isNotNull();
        assertThat(os.name()).isNotBlank();
        assertThat(os.version()).isNotBlank();
        assertThat(os.arch()).isNotBlank();
    }

    @TestConfiguration
    static class AxelixDetailsEndpointTestConfig {

        @Bean
        public AxelixDetailsEndpoint axelixDetailsEndpoint(ServiceDetailsAssembler serviceDetailsAssembler) {
            return new AxelixDetailsEndpoint(serviceDetailsAssembler);
        }
    }
}
