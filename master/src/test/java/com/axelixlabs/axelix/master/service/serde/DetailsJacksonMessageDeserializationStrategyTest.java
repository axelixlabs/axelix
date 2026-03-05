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
package com.axelixlabs.axelix.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.InstanceDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.BuildDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.GitDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.OsDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.RuntimeDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.SpringDetails;

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
    void shouldDeserializeAxelixDetails() {
        // language=json
        String response = """
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
        InstanceDetails instanceDetails = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        GitDetails git = instanceDetails.getGit();
        assertThat(git.getCommitShaShort()).isEqualTo("7a663cb");
        assertThat(git.getBranch()).isEqualTo("local/local-test");
        assertThat(git.getCommitAuthor().getName()).isEqualTo("Mikhail Polivakha");
        assertThat(git.getCommitAuthor().getEmail()).isEqualTo("mikhailpolivakha@github.com");
        assertThat(git.getCommitTimestamp()).isEqualTo("2025-11-23T02:25:22Z");

        SpringDetails spring = instanceDetails.getSpring();
        assertThat(spring.getSpringBootVersion()).isEqualTo("3.5.0");
        assertThat(spring.getSpringFrameworkVersion()).isEqualTo("7.0");
        assertThat(spring.getSpringCloudVersion()).isEqualTo("2023.0.1");

        RuntimeDetails runtime = instanceDetails.getRuntime();
        assertThat(runtime.getJavaVersion()).isEqualTo("17.0.16");
        assertThat(runtime.getJdkVendor()).isEqualTo("Corretto-17.0.16.8.1");
        assertThat(runtime.getGarbageCollector()).isEqualTo("G1 GC");
        assertThat(runtime.getKotlinVersion()).isEqualTo("1.9.0");

        BuildDetails build = instanceDetails.getBuild();
        assertThat(build.getArtifact()).isEqualTo("spring-petclinic");
        assertThat(build.getVersion()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(build.getGroup()).isEqualTo("org.springframework.samples");
        assertThat(build.getTime()).isEqualTo("2025-10-29T15:10:54.770Z");

        OsDetails os = instanceDetails.getOs();
        assertThat(os.getName()).isEqualTo("Windows 10");
        assertThat(os.getVersion()).isEqualTo("10.0");
        assertThat(os.getArch()).isEqualTo("amd64");
    }
}
