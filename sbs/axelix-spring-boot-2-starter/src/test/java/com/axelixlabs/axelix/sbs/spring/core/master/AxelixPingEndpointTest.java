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
package com.axelixlabs.axelix.sbs.spring.core.master;

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.HealthStatus;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestInsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixPingEndpoint}.
 *
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({AxelixPingEndpoint.class, AxelixPingEndpointTest.CurrentConfig.class, JwtAuthTestConfiguration.class})
class AxelixPingEndpointTest {

    @Autowired
    private TestRestTemplateBuilder testRestTemplate;

    @TestConfiguration
    static class CurrentConfig {

        @Bean
        HealthDetectionFunction healthDetectionFunction() {
            return () -> HealthStatus.UP;
        }

        @Bean
        public InsightsInfoProvider insightsInfoProvider() {
            return new TestInsightsInfoProvider();
        }

        @Bean
        public MasterPingAssembler masterPingAssembler(
                HealthDetectionFunction healthDetectionFunction, InsightsInfoProvider insightsInfoProvider) {
            return new MasterPingAssembler(healthDetectionFunction, insightsInfoProvider);
        }
    }

    @Test
    void shouldReceivePing() {
        // when.
        ResponseEntity<String> result = testRestTemplate.asViewer().getForEntity("/actuator/axelix-ping", String.class);

        // then.
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        JsonAssertions.assertThatJson(result.getBody())
                .isEqualTo("{\n" + "  \"healthStatus\" : \"UP\",\n"
                        + "  \"memoryDetails\" : {\n"
                        + "    \"heap\" : \"#{json-unit.ignore}\"\n"
                        + "  },\n"
                        + "  \"transactions\" : [ ]\n"
                        + "}");
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-ping")
    void negativeAuthTests() {}
}
