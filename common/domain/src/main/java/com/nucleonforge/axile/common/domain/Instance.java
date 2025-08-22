package com.nucleonforge.axile.common.domain;

import java.util.Objects;

public class Instance {

    /**
     * The id of the instance. This id must be unique among all the other instances that are
     * managed by this Axile Master.
     */
    private final String id;

    /**
     * Build information of the given Instance.
     */
    private final BuildInfo buildInfo;

    /**
     * Classes loaded by this application.
     */
    private final LoadedClasses loadedClasses;

    /**
     * Details of the launch of the given {@link Instance}.
     */
    private final LaunchDetails launchDetails;

    public Instance(String id, BuildInfo buildInfo, LoadedClasses loadedClasses, LaunchDetails launchDetails) {
        this.id = id;
        this.buildInfo = buildInfo;
        this.loadedClasses = loadedClasses;
        this.launchDetails = launchDetails;
    }

    public String getId() {
        return id;
    }

    public BuildInfo getBuildInfo() {
        return buildInfo;
    }

    public LoadedClasses getLoadedClasses() {
        return loadedClasses;
    }

    public LaunchDetails getLaunchDetails() {
        return launchDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Instance instance)) {
            return false;
        }
        return Objects.equals(id, instance.id)
                && Objects.equals(buildInfo, instance.buildInfo)
                && Objects.equals(loadedClasses, instance.loadedClasses)
                && Objects.equals(launchDetails, instance.launchDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, buildInfo, loadedClasses, launchDetails);
    }
}
