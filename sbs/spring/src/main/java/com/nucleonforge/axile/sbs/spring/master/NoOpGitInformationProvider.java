package com.nucleonforge.axile.sbs.spring.master;

import java.util.Optional;

import com.nucleonforge.axile.common.api.registration.GitInfo;

/**
 * The NoOp implementation of {@link GitInformationProvider}. Mainly serves as the fallback in case
 * no other valid {@link GitInformationProvider} found.
 *
 * @author Mikhail Polivakha
 */
public class NoOpGitInformationProvider implements GitInformationProvider {

    @Override
    public Optional<GitInfo> getGitCommitInfo() {
        return Optional.empty();
    }
}
