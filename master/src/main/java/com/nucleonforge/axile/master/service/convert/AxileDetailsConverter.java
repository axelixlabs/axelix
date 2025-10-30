package com.nucleonforge.axile.master.service.convert;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

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

/**
 * The {@link Converter} from {@link AxileDetails} to {@link AxileDetailsResponse}.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
@Service
public class AxileDetailsConverter implements Converter<AxileDetails, AxileDetailsResponse> {

    @Override
    public @NonNull AxileDetailsResponse convertInternal(@NonNull AxileDetails source) {
        String serviceName = source.instanceName();
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
