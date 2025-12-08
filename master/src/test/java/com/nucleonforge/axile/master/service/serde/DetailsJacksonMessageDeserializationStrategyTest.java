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
package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.common.api.AxileDetails.BuildDetails;
import com.nucleonforge.axile.common.api.AxileDetails.GitDetails;
import com.nucleonforge.axile.common.api.AxileDetails.OsDetails;
import com.nucleonforge.axile.common.api.AxileDetails.RuntimeDetails;
import com.nucleonforge.axile.common.api.AxileDetails.SpringDetails;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DetailsJacksonMessageDeserializationStrategy}.
 *
 * @author Sergey Cherkasov
 */
public class DetailsJacksonMessageDeserializationStrategyTest {

    private final DetailsJacksonMessageDeserializationStrategy subject =
            new DetailsJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeAxileDetails() {
        // language=json
        String response =
                """
            {
             "git": {
                 "commitShaShort": "7a663cb",
                 "branch": "local/local-test",
                 "commitAuthor": {
                     "name": "Mikhail Polivakha",
                     "email": "mikhailpolivakha@github.com"
                 },
                 "commitTimestamp": "2025-11-23T02:25:22Z"
             },
             "spring": {
                 "springBootVersion": "3.5.0",
                 "springFrameworkVersion": "7.0",
                 "springCloudVersion": "2023.0.1"
             },
             "runtime": {
                 "javaVersion": "17.0.16",
                 "jdkVendor": "Corretto-17.0.16.8.1",
                 "garbageCollector": "G1 GC",
                 "kotlinVersion": "1.9.0"
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

        // when.
        AxileDetails axileDetails = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        GitDetails git = axileDetails.git();
        assertThat(git.commitShaShort()).isEqualTo("7a663cb");
        assertThat(git.branch()).isEqualTo("local/local-test");
        assertThat(git.commitAuthor().name()).isEqualTo("Mikhail Polivakha");
        assertThat(git.commitAuthor().email()).isEqualTo("mikhailpolivakha@github.com");
        assertThat(git.commitTimestamp()).isEqualTo("2025-11-23T02:25:22Z");

        SpringDetails spring = axileDetails.spring();
        assertThat(spring.springBootVersion()).isEqualTo("3.5.0");
        assertThat(spring.springFrameworkVersion()).isEqualTo("7.0");
        assertThat(spring.springCloudVersion()).isEqualTo("2023.0.1");

        RuntimeDetails runtime = axileDetails.runtime();
        assertThat(runtime.javaVersion()).isEqualTo("17.0.16");
        assertThat(runtime.jdkVendor()).isEqualTo("Corretto-17.0.16.8.1");
        assertThat(runtime.garbageCollector()).isEqualTo("G1 GC");
        assertThat(runtime.kotlinVersion()).isEqualTo("1.9.0");

        BuildDetails build = axileDetails.build();
        assertThat(build.artifact()).isEqualTo("spring-petclinic");
        assertThat(build.version()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(build.group()).isEqualTo("org.springframework.samples");
        assertThat(build.time()).isEqualTo("2025-10-29T15:10:54.770Z");

        OsDetails os = axileDetails.os();
        assertThat(os.name()).isEqualTo("Windows 10");
        assertThat(os.version()).isEqualTo("10.0");
        assertThat(os.arch()).isEqualTo("amd64");
    }
}
