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
package com.axelixlabs.axelix.sbs.spring.core.sbom;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.IgnoreTestContextArchitecture;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.sbs.spring.core.IgnoreTestContextArchitecture.NO_SIBLINGS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixDependenciesEndpoint}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JwtAuthTestConfiguration.class)
@IgnoreTestContextArchitecture(reason = NO_SIBLINGS)
class AxelixDependenciesEndpointTest {

    /**
     * The test-classpath stand-in for the resource the Axelix build plugins package into the archive.
     */
    private static final String PRESENT_RESOURCE = "axelix/sbom-test/dependencies.cdx.json";

    @Autowired
    private TestRestTemplateBuilder testRestTemplate;

    @TestConfiguration
    static class AxelixDependenciesEndpointTestConfiguration {

        @Bean
        public ClasspathDependencySbom classpathDependencySbom() {
            return new ClasspathDependencySbom(PRESENT_RESOURCE);
        }

        @Bean
        public AxelixDependenciesEndpoint axelixDependenciesEndpoint(ClasspathDependencySbom classpathDependencySbom) {
            return new AxelixDependenciesEndpoint(classpathDependencySbom);
        }
    }

    @Test
    void shouldServeTheSbomAsJson() {

        // when.
        ResponseEntity<String> response =
                testRestTemplate.asViewer().getForEntity("/actuator/axelix-dependencies", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).contains("\"bomFormat\": \"CycloneDX\"").contains("\"specVersion\": \"1.6\"");
    }

    @Test
    void shouldRespondNoContentWhenTheApplicationServesNoSbom() {

        // given.
        AxelixDependenciesEndpoint endpoint =
                new AxelixDependenciesEndpoint(new ClasspathDependencySbom("axelix/sbom-test/no-such-file.cdx.json"));

        // when.
        ResponseEntity<byte[]> response = endpoint.sbom();

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-dependencies")
    void negativeAuthTests() {}
}
