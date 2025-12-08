/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.service.convert;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.common.api.AxileDetails.BuildDetails;
import com.nucleonforge.axile.common.api.AxileDetails.GitDetails;
import com.nucleonforge.axile.common.api.AxileDetails.OsDetails;
import com.nucleonforge.axile.common.api.AxileDetails.RuntimeDetails;
import com.nucleonforge.axile.common.api.AxileDetails.SpringDetails;
import com.nucleonforge.axile.master.api.response.AxileDetailsResponse;
import com.nucleonforge.axile.master.api.response.AxileDetailsResponse.BuildProfile;
import com.nucleonforge.axile.master.api.response.AxileDetailsResponse.GitProfile;
import com.nucleonforge.axile.master.api.response.AxileDetailsResponse.OSProfile;
import com.nucleonforge.axile.master.api.response.AxileDetailsResponse.RuntimeProfile;
import com.nucleonforge.axile.master.api.response.AxileDetailsResponse.SpringProfile;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.response.details.AxileDetailsConverter;
import com.nucleonforge.axile.master.service.convert.response.details.DetailsConversionRequest;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AxileDetailsConverter}
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
public class AxileDetailsConverterTest {

    @Autowired
    private InstanceRegistry instanceRegistry;

    private final String activeInstanceId = UUID.randomUUID().toString();

    private AxileDetailsConverter converter;

    @BeforeEach
    void prepare() {
        instanceRegistry.register(createInstance(activeInstanceId));
        converter = new AxileDetailsConverter(instanceRegistry);
    }

    @Test
    void testConvertHappyPath() {
        // when.
        AxileDetailsResponse response = converter.convertInternal(
                new DetailsConversionRequest(getAxileDetails(), InstanceId.of(activeInstanceId)));

        assertThat(response.serviceName()).isEqualTo("test-object-factory-instance");

        GitProfile git = response.git();
        assertThat(git).isNotNull();
        assertThat(git.commitShaShort()).isEqualTo("7a663cb");
        assertThat(git.branch()).isEqualTo("local/local-test");
        assertThat(git.authorName()).isEqualTo("sergeycherkasovv");
        assertThat(git.authorEmail()).isEqualTo("sergeycherkasovv@github.com");
        assertThat(git.commitTimestamp()).isEqualTo("2025-11-23T02:25:22Z");

        SpringProfile spring = response.spring();
        assertThat(spring.springBootVersion()).isEqualTo("3.5.0");
        assertThat(spring.springFrameworkVersion()).isEqualTo("7.0");
        assertThat(spring.springCloudVersion()).isEqualTo("2023.0.1");

        RuntimeProfile runtime = response.runtime();
        assertThat(runtime.javaVersion()).isEqualTo("17.0.16");
        assertThat(runtime.jdkVendor()).isEqualTo("Corretto-17.0.16.8.1");
        assertThat(runtime.garbageCollector()).isEqualTo("G1 GC");
        assertThat(runtime.kotlinVersion()).isEqualTo(null);

        BuildProfile build = response.build();
        assertThat(build).isNotNull();
        assertThat(build.artifact()).isEqualTo("spring-petclinic");
        assertThat(build.version()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(build.group()).isEqualTo("org.springframework.samples");
        assertThat(build.time()).isEqualTo("2025-10-29T15:10:54.770Z");

        OSProfile os = response.os();
        assertThat(os.name()).isEqualTo("Windows 10");
        assertThat(os.version()).isEqualTo("10.0");
        assertThat(os.arch()).isEqualTo("amd64");
    }

    private static AxileDetails getAxileDetails() {
        GitDetails.CommitAuthor commitAuthor =
                new AxileDetails.GitDetails.CommitAuthor("sergeycherkasovv", "sergeycherkasovv@github.com");

        GitDetails gitDetails =
                new AxileDetails.GitDetails("7a663cb", "local/local-test", commitAuthor, "2025-11-23T02:25:22Z");

        SpringDetails springDetails = new AxileDetails.SpringDetails("3.5.0", "7.0", "2023.0.1");

        RuntimeDetails runtimeDetails =
                new AxileDetails.RuntimeDetails("17.0.16", "Corretto-17.0.16.8.1", "G1 GC", null);

        BuildDetails buildDetails = new AxileDetails.BuildDetails(
                "spring-petclinic", "3.5.0-SNAPSHOT", "org.springframework.samples", "2025-10-29T15:10:54.770Z");

        OsDetails osDetails = new AxileDetails.OsDetails("Windows 10", "10.0", "amd64");

        return new AxileDetails(gitDetails, springDetails, runtimeDetails, buildDetails, osDetails);
    }
}
