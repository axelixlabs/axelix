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
        GitProfile gitProfile = gitDetailsConverter(source.getGit());
        RuntimeProfile runtimeProfile = runtimeDetailsConverter(source.getRuntime());
        SpringProfile springProfile = springDetailsConverter(source.getSpring());
        BuildProfile buildProfile = buildDetailsConverter(source.getBuild());
        OSProfile osProfile = osDetailsConverter(source.getOs());

        return new InstanceDetailsResponse(
                serviceName, gitProfile, runtimeProfile, springProfile, buildProfile, osProfile, instance.vmFeatures());
    }

    private GitProfile gitDetailsConverter(GitDetails gitDetails) {
        return new InstanceDetailsResponse.GitProfile(
                gitDetails.getCommitShaShort(),
                gitDetails.getBranch(),
                gitDetails.getCommitAuthor().getName(),
                gitDetails.getCommitAuthor().getEmail(),
                gitDetails.getCommitTimestamp());
    }

    private RuntimeProfile runtimeDetailsConverter(RuntimeDetails runtimeDetails) {
        return new RuntimeProfile(
                runtimeDetails.getJavaVersion(),
                runtimeDetails.getKotlinVersion(),
                runtimeDetails.getJdkVendor(),
                runtimeDetails.getGarbageCollector());
    }

    private SpringProfile springDetailsConverter(SpringDetails springDetails) {
        return new SpringProfile(
                springDetails.getSpringBootVersion(),
                springDetails.getSpringFrameworkVersion(),
                springDetails.getSpringCloudVersion());
    }

    private BuildProfile buildDetailsConverter(BuildDetails buildDetails) {
        return new InstanceDetailsResponse.BuildProfile(
                buildDetails.getArtifact(), buildDetails.getVersion(), buildDetails.getGroup(), buildDetails.getTime());
    }

    private OSProfile osDetailsConverter(OsDetails osDetails) {
        return new OSProfile(osDetails.getName(), osDetails.getVersion(), osDetails.getArch());
    }
}
