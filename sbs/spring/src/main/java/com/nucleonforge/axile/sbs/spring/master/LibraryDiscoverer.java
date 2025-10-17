package com.nucleonforge.axile.sbs.spring.master;

import java.util.Optional;

/**
 * Interface that is capable to discover the given library in the classpath.
 *
 * @author Mikhail Polivakha
 */
public interface LibraryDiscoverer {

    /**
     * Detect library version.
     *
     * @param artifactId the artifact id of the dependency
     * @param groupId the groupId of the dependency
     * @return the library version, or {@link Optional#empty()} if none were detected
     */
    Optional<String> getLibraryVersion(String artifactId, String groupId);
}
