package com.nucleonforge.axile.master.service.versions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.BuildInfo;
import com.nucleonforge.axile.master.model.software.SoftwareComponent;
import com.nucleonforge.axile.master.model.software.SoftwareDistribution;

/**
 * The SPI interface that is capable to extract the information about specific {@link SoftwareDistribution}
 * inside the given {@link BuildInfo} about the given {@link SoftwareComponent}.
 *
 * @author Mikhail Polivakha
 */
public interface SoftwareDistributionDiscoverer<T extends SoftwareComponent> {

    @Nullable
    SoftwareDistribution discover(@NonNull BuildInfo buildInfo);
}
