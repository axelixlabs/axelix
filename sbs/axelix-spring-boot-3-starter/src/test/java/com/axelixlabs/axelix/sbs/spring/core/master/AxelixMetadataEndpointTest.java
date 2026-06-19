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
 * Integration tests for {@link AxelixMetadataEndpoint}.
 *
 * @author Mikhail Polivakha
 * @author Artemiy Degtyarev
 */
class AxelixMetadataEndpointTest extends AbstractMasterSharedContextTest {

    @Autowired
    private TestRestTemplateBuilder testRestTemplate;

    @Test
    void shouldReceiveServiceMetadata() {
        // when.
        ResponseEntity<String> result =
                testRestTemplate.asViewer().getForEntity("/actuator/axelix-metadata", String.class);

        // then.
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        JsonAssertions.assertThatJson(result.getBody())
                // we do not want to know exactly the java version on which the test is going to run
                .whenIgnoringPaths("softwareVersions", "vmFeatures")
                .isEqualTo(
                        // language=json
                        """
            {
              "version": "1.1.3",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "a8b0929",
              "jdkVendor" : "#{json-unit.ignore}",
              "softwareVersions" : {
                "springBoot" : "3.5.0",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "UP",
              "memoryDetails" : {
                "heap" : "#{json-unit.ignore}"
              },
              "vmFeatures": []
            }
            """);
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-metadata")
    void negativeAuthTests() {}
}
