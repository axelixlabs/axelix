package com.nucleonforge.axile.master.model.software.core;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.master.model.software.SoftwareComponent;

public final class JdkDistribution implements SoftwareComponent {

    @Override
    public @NonNull String getName() {
        return "JDK Distribution";
    }

    @Override
    public String getDescription() {
        return "The distribution of JDK being used in the application";
    }

    @Override
    public boolean isCore() {
        return true;
    }
}
