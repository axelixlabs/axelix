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
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixPingEndpoint}.
 *
 * @author Nikita Kirillov
 */
class AxelixPingEndpointTest extends AbstractMasterSharedContextTest {

    @Autowired
    private TestRestTemplateBuilder testRestTemplate;

    @Test
    void shouldReceivePing() {
        // when.
        ResponseEntity<String> result = testRestTemplate.asViewer().getForEntity("/actuator/axelix-ping", String.class);

        // then.
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        JsonAssertions.assertThatJson(result.getBody())
                .isEqualTo(
                        // language=json
                        """
            {
              "healthStatus" : "UP",
              "memoryDetails" : {
                "heap" : "#{json-unit.ignore}"
              },
              "transactions" : [ ]
            }
            """);
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-ping")
    void negativeAuthTests() {}
}
