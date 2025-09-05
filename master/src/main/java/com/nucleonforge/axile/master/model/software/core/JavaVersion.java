package com.nucleonforge.axile.master.model.software.core;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.master.model.software.SoftwareComponent;

public final class JavaVersion implements SoftwareComponent {

    @Override
    public @NonNull String getName() {
        return "Java";
    }

    @Override
    public String getDescription() {
        return "The version of Java language being used in the application";
    }

    @Override
    public boolean isCore() {
        return true;
    }
}
