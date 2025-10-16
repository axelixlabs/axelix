package com.nucleonforge.axile.sbs.spring.master;

import java.util.Optional;

/**
 * The No-Op implementation of {@link LibraryDiscoverer}. Mainly serves as a fallback
 * in case of no other valid {@link LibraryDiscoverer} found.
 *
 * @author Mikhail Polivakha
 */
public class NoOpLibraryDiscoverer implements LibraryDiscoverer {

    @Override
    public Optional<String> getLibraryVersion(String artifactId, String groupId) {
        return Optional.empty();
    }
}
