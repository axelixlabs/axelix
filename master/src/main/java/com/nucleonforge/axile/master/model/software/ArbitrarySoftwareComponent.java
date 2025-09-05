package com.nucleonforge.axile.master.model.software;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Any arbitrary {@link SoftwareComponent versionable component} that is used in the app, like
 * any specific library e.g. jackson, vavr, rabbitmq-client etc.
 *
 * @author Mikhail Polivakha
 */
public record ArbitrarySoftwareComponent(String name, String description, boolean isCore) implements SoftwareComponent {

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public boolean isCore() {
        return isCore;
    }
}
