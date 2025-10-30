package com.nucleonforge.axile.master.service.convert.details;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.details.AxileDetails;
import com.nucleonforge.axile.master.api.response.details.AxileDetailsResponse;
import com.nucleonforge.axile.master.api.response.details.components.BuildProfile;
import com.nucleonforge.axile.master.api.response.details.components.GitProfile;
import com.nucleonforge.axile.master.api.response.details.components.OSProfile;
import com.nucleonforge.axile.master.api.response.details.components.RuntimeProfile;
import com.nucleonforge.axile.master.api.response.details.components.SpringProfile;
import com.nucleonforge.axile.master.model.details.TransitAxileDetails;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.convert.details.components.BuildDetailsConverter;
import com.nucleonforge.axile.master.service.convert.details.components.GitDetailsConverter;
import com.nucleonforge.axile.master.service.convert.details.components.OSDetailsConverter;
import com.nucleonforge.axile.master.service.convert.details.components.RuntimeDetailsConverter;
import com.nucleonforge.axile.master.service.convert.details.components.SpringDetailsConverter;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * The {@link Converter} from {@link TransitAxileDetails} to {@link AxileDetailsResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class AxileDetailsConverter implements Converter<TransitAxileDetails, AxileDetailsResponse> {
    @Autowired
    private InstanceRegistry instanceRegistry;

    private final BuildDetailsConverter buildDetailsConverter;
    private final GitDetailsConverter gitDetailsConverter;
    private final OSDetailsConverter osDetailsConverter;
    private final RuntimeDetailsConverter runtimeDetailsConverter;
    private final SpringDetailsConverter springDetailsConverter;

    public AxileDetailsConverter(
            GitDetailsConverter gitDetailsConverter,
            RuntimeDetailsConverter runtimeDetailsConverter,
            SpringDetailsConverter springDetailsConverter,
            BuildDetailsConverter buildDetailsConverter,
            OSDetailsConverter osDetailsConverter) {
        this.gitDetailsConverter = gitDetailsConverter;
        this.runtimeDetailsConverter = runtimeDetailsConverter;
        this.springDetailsConverter = springDetailsConverter;
        this.buildDetailsConverter = buildDetailsConverter;
        this.osDetailsConverter = osDetailsConverter;
    }

    @Override
    public @NonNull AxileDetailsResponse convertInternal(@NonNull TransitAxileDetails source) {
        AxileDetails axileDetails = source.axileDetails();
        GitProfile gitProfile = gitDetailsConverter.convert(axileDetails.git());
        RuntimeProfile runtimeProfile = runtimeDetailsConverter.convert(axileDetails.runtime());
        SpringProfile springProfile = springDetailsConverter.convert(axileDetails.spring());
        BuildProfile buildProfile = buildDetailsConverter.convert(axileDetails.build());
        OSProfile osProfile = osDetailsConverter.convert(axileDetails.os());

        String serviceName = source.serviceName();

        return new AxileDetailsResponse(
                serviceName, gitProfile, runtimeProfile, springProfile, buildProfile, osProfile);
    }
}
