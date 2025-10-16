package com.nucleonforge.axile.sbs.spring.master;

import java.util.Optional;

import com.nucleonforge.axile.common.api.registration.ShortBuildInfo;

/**
 * A NoOp implementation of {@link ShortBuildInfoProvider}. Mainly serves as a fallback in case
 * no other valud {@link ShortBuildInfoProvider} were deemed applicable.
 *
 * @author Mikhail Polivakha
 */
public class NoOpShortBuildInfoProvider implements ShortBuildInfoProvider {

    @Override
    public Optional<ShortBuildInfo> getShortBuildInfo() {
        return Optional.empty();
    }
}
