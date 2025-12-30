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
package com.nucleonforge.axelix.master.service.transport;

import java.io.IOException;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axelix.common.api.InstanceDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.BuildDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.GitDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.OsDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.RuntimeDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.SpringDetails;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

import static com.nucleonforge.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DetailsEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class DetailsEndpointProberTest {
    private final String activeInstanceId = UUID.randomUUID().toString();

    private MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private DetailsEndpointProber detailsEndpointProber;

    @BeforeEach
    void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void prepare() {
        // language=json
        String response =
                """
            {
             "git": {
                 "commitShaShort": "7a663cb",
                 "branch": "local/local-test",
                 "commitAuthor": {
                     "name": "Ashot Sargsyan",
                     "email": "AshotSargsyan@github.com"
                 },
                 "commitTimestamp": "2025-11-23T02:25:22Z"
             },
             "spring": {
                 "springBootVersion": "3.5.0",
                 "springFrameworkVersion": "7.0"
             },
             "runtime": {
                 "javaVersion": "17.0.16",
                 "jdkVendor": "Corretto-17.0.16.8.1",
                 "garbageCollector": "G1 GC"
             },
             "build": {
                 "artifact": "spring-petclinic",
                 "version": "3.5.0-SNAPSHOT",
                 "group": "org.springframework.samples",
                 "time": "2025-10-29T15:10:54.770Z"
             },
             "os": {
                 "name": "Windows 10",
                 "version": "10.0",
                 "arch": "amd64"
             }
        }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-details")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldReturnaxelixDetailsResponse() {
        // when.
        registry.register(createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
        InstanceDetails instanceDetails =
                detailsEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        GitDetails git = instanceDetails.git();
        assertThat(git.commitShaShort()).isEqualTo("7a663cb");
        assertThat(git.branch()).isEqualTo("local/local-test");
        assertThat(git.commitAuthor().name()).isEqualTo("Ashot Sargsyan");
        assertThat(git.commitAuthor().email()).isEqualTo("AshotSargsyan@github.com");
        assertThat(git.commitTimestamp()).isEqualTo("2025-11-23T02:25:22Z");

        SpringDetails spring = instanceDetails.spring();
        assertThat(spring.springBootVersion()).isEqualTo("3.5.0");
        assertThat(spring.springFrameworkVersion()).isEqualTo("7.0");
        assertThat(spring.springCloudVersion()).isNull();

        RuntimeDetails runtime = instanceDetails.runtime();
        assertThat(runtime.javaVersion()).isEqualTo("17.0.16");
        assertThat(runtime.jdkVendor()).isEqualTo("Corretto-17.0.16.8.1");
        assertThat(runtime.garbageCollector()).isEqualTo("G1 GC");
        assertThat(runtime.kotlinVersion()).isNull();

        BuildDetails build = instanceDetails.build();
        assertThat(build.artifact()).isEqualTo("spring-petclinic");
        assertThat(build.version()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(build.group()).isEqualTo("org.springframework.samples");
        assertThat(build.time()).isEqualTo("2025-10-29T15:10:54.770Z");

        OsDetails os = instanceDetails.os();
        assertThat(os.name()).isEqualTo("Windows 10");
        assertThat(os.version()).isEqualTo("10.0");
        assertThat(os.arch()).isEqualTo("amd64");
    }
}
