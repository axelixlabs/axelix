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
package com.axelixlabs.axelix.master.service.convert;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.axelixlabs.axelix.common.api.InstanceDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.BuildDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.GitDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.OsDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.RuntimeDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.SpringDetails;
import com.axelixlabs.axelix.master.api.external.response.InstanceDetailsResponse;
import com.axelixlabs.axelix.master.api.external.response.InstanceDetailsResponse.BuildProfile;
import com.axelixlabs.axelix.master.api.external.response.InstanceDetailsResponse.GitProfile;
import com.axelixlabs.axelix.master.api.external.response.InstanceDetailsResponse.OSProfile;
import com.axelixlabs.axelix.master.api.external.response.InstanceDetailsResponse.RuntimeProfile;
import com.axelixlabs.axelix.master.api.external.response.InstanceDetailsResponse.SpringProfile;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.convert.response.details.DetailsConversionRequest;
import com.axelixlabs.axelix.master.service.convert.response.details.InstanceDetailsConverter;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link InstanceDetailsConverter}
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
public class InstanceDetailsConverterTest {

    @Autowired
    private InstanceRegistry instanceRegistry;

    private final String activeInstanceId = UUID.randomUUID().toString();

    private InstanceDetailsConverter converter;

    @BeforeEach
    void prepare() {
        instanceRegistry.register(createInstance(activeInstanceId));
        converter = new InstanceDetailsConverter(instanceRegistry);
    }

    @Test
    void testConvertHappyPath() {
        // when.
        InstanceDetailsResponse response = converter.convertInternal(
                new DetailsConversionRequest(getInstanceDetails(), InstanceId.of(activeInstanceId)));

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

    private static InstanceDetails getInstanceDetails() {
        GitDetails.CommitAuthor commitAuthor =
                new InstanceDetails.GitDetails.CommitAuthor("sergeycherkasovv", "sergeycherkasovv@github.com");

        GitDetails gitDetails =
                new InstanceDetails.GitDetails("7a663cb", "local/local-test", commitAuthor, "2025-11-23T02:25:22Z");

        SpringDetails springDetails = new InstanceDetails.SpringDetails("3.5.0", "7.0", "2023.0.1");

        RuntimeDetails runtimeDetails =
                new InstanceDetails.RuntimeDetails("17.0.16", "Corretto-17.0.16.8.1", "G1 GC", null);

        BuildDetails buildDetails = new InstanceDetails.BuildDetails(
                "spring-petclinic", "3.5.0-SNAPSHOT", "org.springframework.samples", "2025-10-29T15:10:54.770Z");

        OsDetails osDetails = new InstanceDetails.OsDetails("Windows 10", "10.0", "amd64");

        return new InstanceDetails(gitDetails, springDetails, runtimeDetails, buildDetails, osDetails);
    }
}
