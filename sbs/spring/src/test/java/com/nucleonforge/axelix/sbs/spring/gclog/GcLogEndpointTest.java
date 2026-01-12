/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.gclog;

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

import com.nucleonforge.axelix.common.api.gclog.GcLogEnableRequest;
import com.nucleonforge.axelix.common.api.gclog.GcLogStatusResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(GcLogEndpointTest.GcLogEndpointTestConfiguration.class)
class GcLogEndpointTest {

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
        assertThat(gcLogStatusResponse.enabled()).isFalse();
        assertThat(gcLogStatusResponse.level()).isNull();
        assertThat(gcLogStatusResponse.availableLevels()).isNotEmpty().doesNotContain("off");
    }

    @Test
    void enable_shouldEnableGcLogging() {
        List<String> availableLevels = getStatus().availableLevels();

        GcLogEnableRequest request = new GcLogEnableRequest(availableLevels.get(0));

        ResponseEntity<Void> response =
                restTemplate.postForEntity("/actuator/axelix-gc/log/enable", request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        GcLogStatusResponse status = getStatus();
        assertThat(status.enabled()).isTrue();
        assertThat(status.level()).isEqualTo(request.level());
    }

    @Test
    void disable_shouldDisableGcLogging() {
        List<String> availableLevels = getStatus().availableLevels();
        GcLogEnableRequest enableRequest = new GcLogEnableRequest(availableLevels.get(0));

        ResponseEntity<Void> enableResponse =
                restTemplate.postForEntity("/actuator/axelix-gc/log/enable", enableRequest, Void.class);

        assertThat(enableResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> disableResponse =
                restTemplate.postForEntity("/actuator/axelix-gc/log/disable", null, Void.class);

        assertThat(disableResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        GcLogStatusResponse status = getStatus();
        assertThat(status.enabled()).isFalse();
        assertThat(status.level()).isNull();
    }

    @Test
    void gcLogfile_shouldReturnFileWhenLoggingEnabled() throws InterruptedException {
        List<String> availableLevels = getStatus().availableLevels();
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
    void gcLogfile_shouldReturnErrorWhenFileNotFound() throws IOException, InterruptedException {
        // In afterAll() method log files are deleted
        afterAll();

        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axelix-gclog/gc-logfile", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
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
    static class GcLogEndpointTestConfiguration {

        @Bean
        public JcmdExecutor jcmdExecutor() {
            return new JcmdExecutor();
        }

        @Bean
        public GcLogService gcLogService(JcmdExecutor jcmdExecutor) {
            return new DefaultGcLogService(jcmdExecutor);
        }

        @Bean
        public GcLogEndpoint gcLogEndpoint(GcLogService gcLogService) {
            return new GcLogEndpoint(gcLogService);
        }
    }
}
