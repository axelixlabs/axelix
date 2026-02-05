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
package com.axelixlabs.axelix.master.api.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SelfRegisteredApi}
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"axelix.master.discovery.auto=false"})
public class SelfRegisteredApiTest {
    // language=json
    private static final String JSON_REQUEST =
            """
        {
       "basicDiscoveryMetadata" : {
         "version": "1.0.0-SNAPSHOT",
         "serviceVersion" : "3.5.0-SNAPSHOT",
         "commitShortSha" : "a8b0929",
         "jdkVendor" : "BellSoft",
         "softwareVersions" : {
           "springBoot" : "3.5.0",
           "java" : "25",
           "springFramework" : "6.1.2",
           "kotlin" : null
         },
         "healthStatus" : "UP",
         "memoryDetails" : {
           "heap" : 12000
         },
         "vmFeatures": [
           {
             "name" : "AppCDS",
             "description" : "AppCDS Description",
             "enabled" : false
           }
         ]
       },
         "instanceId" : "3c994958-924f-4a12-87d0-a8782e97af10",
         "instanceName" : "petclinic",
         "instanceURL" : "http://localhost:8080",
         "deploymentAt" : "2025-02-03T13:29:29Z"
     }
    """;

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Test
    void shouldRegistryServiceInstance() {
        // when.
        ResponseEntity<Void> response = restTemplate
                .withAuthorities()
                .postForEntity("/api/axelix/service/register", defaultJsonEntity(JSON_REQUEST), Void.class);

        // then.
        assertThat(response.getStatusCode()).isNotNull().isEqualTo(HttpStatus.NO_CONTENT);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .postForEntity("/api/axelix/service/register", defaultJsonEntity(JSON_REQUEST), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private <T> HttpEntity<T> defaultJsonEntity(T request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }
}
