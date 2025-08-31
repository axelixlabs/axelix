package com.nucleonforge.axile.common.domain;

import java.util.Objects;

import org.jspecify.annotations.NonNull;

public class Instance {

    /**
     * The id of the instance. This id must be unique among all the other instances that are
     * managed by this Axile Master.
     */
    private final InstanceId id;

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

    /**
     * The URL of the actuator root, e.g. {@code https://my-app:6061/actuator}
     */
    @NonNull
    private final String actuatorUrl;

    public Instance(
            InstanceId id,
            BuildInfo buildInfo,
            LoadedClasses loadedClasses,
            LaunchDetails launchDetails,
            @NonNull String actuatorUrl) {
        this.id = id;
        this.buildInfo = buildInfo;
        this.loadedClasses = loadedClasses;
        this.launchDetails = launchDetails;
        this.actuatorUrl = actuatorUrl;
    }

    public InstanceId getId() {
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

    public @NonNull String getActuatorUrl() {
        return actuatorUrl;
    }
}
