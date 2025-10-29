package com.nucleonforge.axile.sbs.spring.details;

import java.util.Optional;

import com.nucleonforge.axile.sbs.spring.master.LibraryDiscoverer;

public class InfoLibraryDiscoverer implements LibraryDiscoverer {

    @Override
    public Optional<String> getLibraryVersion(String artifactId, String groupId) {
        return Optional.empty();
    }
}
