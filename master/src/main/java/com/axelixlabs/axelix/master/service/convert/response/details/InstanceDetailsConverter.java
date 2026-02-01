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
package com.axelixlabs.axelix.master.service.convert.response.details;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.InstanceDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.BuildDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.GitDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.OsDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.RuntimeDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.SpringDetails;
import com.axelixlabs.axelix.master.api.response.InstanceDetailsResponse;
import com.axelixlabs.axelix.master.api.response.InstanceDetailsResponse.BuildProfile;
import com.axelixlabs.axelix.master.api.response.InstanceDetailsResponse.GitProfile;
import com.axelixlabs.axelix.master.api.response.InstanceDetailsResponse.OSProfile;
import com.axelixlabs.axelix.master.api.response.InstanceDetailsResponse.RuntimeProfile;
import com.axelixlabs.axelix.master.api.response.InstanceDetailsResponse.SpringProfile;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * The {@link Converter} from {@link InstanceDetails} to {@link InstanceDetailsResponse}.
 *
 * @author Nikita Kirilov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
// TODO: Some of the information that we request from axelix-details endpoint is actually
//  available in the Instance itself. But it is also present in the axelix-details endpoint response.
//  What should we do about it?
@Service
public class InstanceDetailsConverter implements Converter<DetailsConversionRequest, InstanceDetailsResponse> {

    private final InstanceRegistry instanceRegistry;

    public InstanceDetailsConverter(InstanceRegistry instanceRegistry) {
        this.instanceRegistry = instanceRegistry;
    }

    @Override
    public @NonNull InstanceDetailsResponse convertInternal(@NonNull DetailsConversionRequest request) {
        InstanceDetails source = request.instanceDetails();
        InstanceId instanceId = request.instanceId();

        Instance instance = instanceRegistry.get(instanceId).orElseThrow(InstanceNotFoundException::new);

        String serviceName = instance.name();
        GitProfile gitProfile = gitDetailsConverter(source.git());
        RuntimeProfile runtimeProfile = runtimeDetailsConverter(source.runtime());
        SpringProfile springProfile = springDetailsConverter(source.spring());
        BuildProfile buildProfile = buildDetailsConverter(source.build());
        OSProfile osProfile = osDetailsConverter(source.os());

        return new InstanceDetailsResponse(
                serviceName, gitProfile, runtimeProfile, springProfile, buildProfile, osProfile, instance.vmFeatures());
    }

    private GitProfile gitDetailsConverter(GitDetails gitDetails) {
        return new InstanceDetailsResponse.GitProfile(
                gitDetails.commitShaShort(),
                gitDetails.branch(),
                gitDetails.commitAuthor().name(),
                gitDetails.commitAuthor().email(),
                gitDetails.commitTimestamp());
    }

    private RuntimeProfile runtimeDetailsConverter(RuntimeDetails runtimeDetails) {
        return new RuntimeProfile(
                runtimeDetails.javaVersion(),
                runtimeDetails.kotlinVersion(),
                runtimeDetails.jdkVendor(),
                runtimeDetails.garbageCollector());
    }

    private SpringProfile springDetailsConverter(SpringDetails springDetails) {
        return new SpringProfile(
                springDetails.springBootVersion(),
                springDetails.springFrameworkVersion(),
                springDetails.springCloudVersion());
    }

    private BuildProfile buildDetailsConverter(BuildDetails buildDetails) {
        return new InstanceDetailsResponse.BuildProfile(
                buildDetails.artifact(), buildDetails.version(), buildDetails.group(), buildDetails.time());
    }

    private OSProfile osDetailsConverter(OsDetails osDetails) {
        return new OSProfile(osDetails.name(), osDetails.version(), osDetails.arch());
    }
}
