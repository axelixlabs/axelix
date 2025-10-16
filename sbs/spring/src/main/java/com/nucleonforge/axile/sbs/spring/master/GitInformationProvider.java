package com.nucleonforge.axile.sbs.spring.master;

import java.util.Optional;

import com.nucleonforge.axile.common.api.registration.GitInfo;

/**
 * The SPI interface that is capable to discover the git commit information.
 *
 * @author Mikhail Polivakha
 */
public interface GitInformationProvider {

    /**
     * @return the {@link GitInfo} discovered from the environment
     */
    Optional<GitInfo> getGitCommitInfo();
}
