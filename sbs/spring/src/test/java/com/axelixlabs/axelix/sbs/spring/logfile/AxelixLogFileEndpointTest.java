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
package com.axelixlabs.axelix.sbs.spring.logfile;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixLogFileEndpoint}
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AxelixLogFileEndpointTest.AxelixLogFileEndpointTestConfiguration.class)
public class AxelixLogFileEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnLogFile_StatusOK() {
        ResponseEntity<Resource> response = restTemplate.getForEntity("/actuator/axelix-log-file", Resource.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("text/plain;charset=UTF-8");
    }

    @Test
    void shouldReturnLogFile_StatusPARTIAL_CONTENT() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Range", "bytes=0-151");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Resource> response =
                restTemplate.exchange("/actuator/axelix-log-file", HttpMethod.GET, entity, Resource.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PARTIAL_CONTENT);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("text/plain;charset=UTF-8");
    }

    @TestConfiguration
    static class AxelixLogFileEndpointTestConfiguration {

        @Bean
        public AxelixLogFileEndpoint axelixLogFileEndpoint(LogFileWebEndpoint logFileWebEndpoint) {
            return new AxelixLogFileEndpoint(logFileWebEndpoint);
        }
    }
}
