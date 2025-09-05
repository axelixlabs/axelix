package com.nucleonforge.axile.master.service.versions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.ClassPathEntry;
import com.nucleonforge.axile.common.domain.Instance;
import com.nucleonforge.axile.common.domain.JarClassPathEntry;
import com.nucleonforge.axile.master.model.software.LibraryComponent;
import com.nucleonforge.axile.master.model.software.SoftwareDistribution;

/**
 * @author Mikhail Polivakha
 */
public abstract class LibrarySoftwareDistributionDiscoverer<T extends LibraryComponent>
        implements SoftwareDistributionDiscoverer<T> {

    private final T lib;

    protected LibrarySoftwareDistributionDiscoverer(@NonNull T lib) {
        this.lib = lib;
    }

    @Override
    public @Nullable SoftwareDistribution discover(@NonNull Instance instance) {
        for (ClassPathEntry classPathEntry : instance.getBuildInfo().getClassPath()) {

            if (classPathEntry instanceof JarClassPathEntry jar) {

                if (jar.getArtifactId().equals(lib.getArtifactId())
                        && jar.getGroupId().equals(lib.getGroupId())) {
                    return new SoftwareDistribution(lib, jar.getVersion());
                }
            }
        }

        return null;
    }
}
