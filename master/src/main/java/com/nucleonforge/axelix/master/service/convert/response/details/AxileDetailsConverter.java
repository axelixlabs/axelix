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

import com.nucleonforge.axelix.common.api.AxileDetails;
import com.nucleonforge.axelix.common.api.AxileDetails.BuildDetails;
import com.nucleonforge.axelix.common.api.AxileDetails.GitDetails;
import com.nucleonforge.axelix.common.api.AxileDetails.OsDetails;
import com.nucleonforge.axelix.common.api.AxileDetails.RuntimeDetails;
import com.nucleonforge.axelix.common.api.AxileDetails.SpringDetails;
import com.nucleonforge.axelix.master.api.response.AxileDetailsResponse;
import com.nucleonforge.axelix.master.api.response.AxileDetailsResponse.BuildProfile;
import com.nucleonforge.axelix.master.api.response.AxileDetailsResponse.GitProfile;
import com.nucleonforge.axelix.master.api.response.AxileDetailsResponse.OSProfile;
import com.nucleonforge.axelix.master.api.response.AxileDetailsResponse.RuntimeProfile;
import com.nucleonforge.axelix.master.api.response.AxileDetailsResponse.SpringProfile;
import com.nucleonforge.axelix.master.exception.InstanceNotFoundException;
import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.convert.response.Converter;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

/**
 * The {@link Converter} from {@link AxileDetails} to {@link AxileDetailsResponse}.
 *
 * @author Nikita Kirilov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
// TODO: Some of the information that we request from axelix-details endpoint is actually
//  available in the Instance itself. But it is also present in the axelix-details endpoint response.
//  What should we do about it?
@Service
public class AxileDetailsConverter implements Converter<DetailsConversionRequest, AxileDetailsResponse> {

    private final InstanceRegistry instanceRegistry;

    public AxileDetailsConverter(InstanceRegistry instanceRegistry) {
        this.instanceRegistry = instanceRegistry;
    }

    @Override
    public @NonNull AxileDetailsResponse convertInternal(@NonNull DetailsConversionRequest request) {
        AxileDetails source = request.axileDetails();
        InstanceId instanceId = request.instanceId();

        Instance instance = instanceRegistry.get(instanceId).orElseThrow(InstanceNotFoundException::new);

        String serviceName = instance.name();
        GitProfile gitProfile = gitDetailsConverter(source.git());
        RuntimeProfile runtimeProfile = runtimeDetailsConverter(source.runtime());
        SpringProfile springProfile = springDetailsConverter(source.spring());
        BuildProfile buildProfile = buildDetailsConverter(source.build());
        OSProfile osProfile = osDetailsConverter(source.os());

        return new AxileDetailsResponse(
                serviceName, gitProfile, runtimeProfile, springProfile, buildProfile, osProfile);
    }

    private GitProfile gitDetailsConverter(GitDetails gitDetails) {
        return new AxileDetailsResponse.GitProfile(
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
        return new AxileDetailsResponse.BuildProfile(
                buildDetails.artifact(), buildDetails.version(), buildDetails.group(), buildDetails.time());
    }

    private OSProfile osDetailsConverter(OsDetails osDetails) {
        return new OSProfile(osDetails.name(), osDetails.version(), osDetails.arch());
    }
}
