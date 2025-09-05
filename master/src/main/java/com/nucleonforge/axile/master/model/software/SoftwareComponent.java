package com.nucleonforge.axile.master.model.software;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Any Software component being used in the system. Can vary from JDK distributions to specific libraries
 * and runtimes.
 *
 * @author Mikhail Polivakha
 */
public interface SoftwareComponent {

    @NonNull
    String getName();

    @Nullable
    String getDescription();

    boolean isCore();
}
