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
package com.nucleonforge.axelix.master.service.convert.response.details;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axelix.common.api.InstanceDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.BuildDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.GitDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.OsDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.RuntimeDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.SpringDetails;
import com.nucleonforge.axelix.master.api.response.InstanceDetailsResponse;
import com.nucleonforge.axelix.master.api.response.InstanceDetailsResponse.BuildProfile;
import com.nucleonforge.axelix.master.api.response.InstanceDetailsResponse.GitProfile;
import com.nucleonforge.axelix.master.api.response.InstanceDetailsResponse.OSProfile;
import com.nucleonforge.axelix.master.api.response.InstanceDetailsResponse.RuntimeProfile;
import com.nucleonforge.axelix.master.api.response.InstanceDetailsResponse.SpringProfile;
import com.nucleonforge.axelix.master.exception.InstanceNotFoundException;
import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.convert.response.Converter;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

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
                serviceName, gitProfile, runtimeProfile, springProfile, buildProfile, osProfile);
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
