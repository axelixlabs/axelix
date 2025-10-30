package com.nucleonforge.axile.master.service.convert;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.master.api.response.AxileDetailsResponse;

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
        AxileDetailsResponse.GitProfile gitProfile = gitDetailsConverter(source.git());
        AxileDetailsResponse.RuntimeProfile runtimeProfile = runtimeDetailsConverter(source.runtime());
        AxileDetailsResponse.SpringProfile springProfile = springDetailsConverter(source.spring());
        AxileDetailsResponse.BuildProfile buildProfile = buildDetailsConverter(source.build());
        AxileDetailsResponse.OSProfile osProfile = osDetailsConverter(source.os());

        return new AxileDetailsResponse(
                serviceName, gitProfile, runtimeProfile, springProfile, buildProfile, osProfile);
    }

    private AxileDetailsResponse.@Nullable GitProfile gitDetailsConverter(
            AxileDetails.@Nullable GitDetails gitDetails) {
        return gitDetails == null
                ? null
                : new AxileDetailsResponse.GitProfile(
                        gitDetails.commitShaShort(),
                        gitDetails.branch(),
                        gitDetails.commitAuthor().name(),
                        gitDetails.commitAuthor().email(),
                        gitDetails.commitTimestamp());
    }

    private AxileDetailsResponse.RuntimeProfile runtimeDetailsConverter(AxileDetails.RuntimeDetails runtimeDetails) {
        return new AxileDetailsResponse.RuntimeProfile(
                runtimeDetails.javaVersion(),
                runtimeDetails.kotlinVersion(),
                runtimeDetails.jdkVendor(),
                runtimeDetails.garbageCollector());
    }

    private AxileDetailsResponse.SpringProfile springDetailsConverter(AxileDetails.SpringDetails springDetails) {
        return new AxileDetailsResponse.SpringProfile(
                springDetails.springBootVersion(),
                springDetails.springFrameworkVersion(),
                springDetails.springCloudVersion());
    }

    private AxileDetailsResponse.@Nullable BuildProfile buildDetailsConverter(
            AxileDetails.@Nullable BuildDetails buildDetails) {
        return buildDetails == null
                ? null
                : new AxileDetailsResponse.BuildProfile(
                        buildDetails.artifact(), buildDetails.version(), buildDetails.group(), buildDetails.time());
    }

    private AxileDetailsResponse.OSProfile osDetailsConverter(AxileDetails.OsDetails osDetails) {
        return new AxileDetailsResponse.OSProfile(osDetails.name(), osDetails.version(), osDetails.arch());
    }
}
