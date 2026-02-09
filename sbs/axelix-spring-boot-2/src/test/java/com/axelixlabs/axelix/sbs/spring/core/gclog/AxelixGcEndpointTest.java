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
package com.axelixlabs.axelix.sbs.spring.core.gclog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.gclog.GcLogEnableRequest;
import com.axelixlabs.axelix.common.api.gclog.GcLogStatusResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AxelixGcEndpointTest.AxelixGcEndpointTestConfiguration.class)
class AxelixGcEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GcLogService gcLogService;

    @AfterEach
    void tearDown() {
        gcLogService.disable();
    }

    @AfterAll
    static void afterAll() throws InterruptedException, IOException {
        Thread.sleep(500);
        Files.deleteIfExists(Path.of("gc.log"));
        Files.deleteIfExists(Path.of("gc.log.0"));
    }

    @Test
    void status_shouldReturnCurrentStatus() {
        ResponseEntity<GcLogStatusResponse> response =
                restTemplate.getForEntity("/actuator/axelix-gc/log/status", GcLogStatusResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        GcLogStatusResponse gcLogStatusResponse = response.getBody();
        assertThat(gcLogStatusResponse.isEnabled()).isFalse();
        assertThat(gcLogStatusResponse.getLevel()).isNull();
        assertThat(gcLogStatusResponse.getAvailableLevels()).isNotEmpty().doesNotContain("off");
    }

    @Test
    void enable_shouldEnableGcLogging() {
        List<String> availableLevels = getStatus().getAvailableLevels();

        GcLogEnableRequest request = new GcLogEnableRequest(availableLevels.get(0));

        ResponseEntity<Void> response =
                restTemplate.postForEntity("/actuator/axelix-gc/log/enable", request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        GcLogStatusResponse status = getStatus();
        assertThat(status.isEnabled()).isTrue();
        assertThat(status.getLevel()).isEqualTo(request.getLevel());
    }

    @Test
    void disable_shouldDisableGcLogging() {
        List<String> availableLevels = getStatus().getAvailableLevels();
        GcLogEnableRequest enableRequest = new GcLogEnableRequest(availableLevels.get(0));

        ResponseEntity<Void> enableResponse =
                restTemplate.postForEntity("/actuator/axelix-gc/log/enable", enableRequest, Void.class);

        assertThat(enableResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> disableResponse =
                restTemplate.postForEntity("/actuator/axelix-gc/log/disable", null, Void.class);

        assertThat(disableResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        GcLogStatusResponse status = getStatus();
        assertThat(status.isEnabled()).isFalse();
        assertThat(status.getLevel()).isNull();
    }

    @Test
    void gcLogfile_shouldReturnFileWhenLoggingEnabled() throws InterruptedException {
        List<String> availableLevels = getStatus().getAvailableLevels();
        GcLogEnableRequest enableRequest = new GcLogEnableRequest(availableLevels.get(0));

        ResponseEntity<Void> enableResponse =
                restTemplate.postForEntity("/actuator/axelix-gc/log/enable", enableRequest, Void.class);

        assertThat(enableResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        System.gc();
        Thread.sleep(500);

        ResponseEntity<byte[]> response = restTemplate.getForEntity("/actuator/axelix-gc/log/file", byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void triggerGc_shouldTriggerGarbageCollection() {
        ResponseEntity<Void> response =
                restTemplate.postForEntity("/actuator/axelix-gc/trigger", HttpEntity.EMPTY, Void.class);

        // Cannot assert GC happened, but endpoint should respond
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void enable_shouldReturnErrorForInvalidLevel() {
        GcLogEnableRequest request = new GcLogEnableRequest("invalid-level");

        ResponseEntity<String> response =
                restTemplate.postForEntity("/actuator/axelix-gc/log/enable", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private GcLogStatusResponse getStatus() {
        ResponseEntity<GcLogStatusResponse> response =
                restTemplate.getForEntity("/actuator/axelix-gc/log/status", GcLogStatusResponse.class);

        return response.getBody();
    }

    @TestConfiguration
    static class AxelixGcEndpointTestConfiguration {

        @Bean
        public JcmdExecutor jcmdExecutor() {
            return new JcmdExecutor();
        }

        @Bean
        public GcLogService gcLogService(JcmdExecutor jcmdExecutor) {
            return new DefaultGcLogService(jcmdExecutor);
        }

        @Bean
        public AxelixGcEndpoint gcLogEndpoint(GcLogService gcLogService) {
            return new AxelixGcEndpoint(gcLogService);
        }
    }
}
