package com.nucleonforge.axile.master.model.software.core;

import com.nucleonforge.axile.master.model.software.LibraryComponent;

/**
 * The Kotlin version used in the app. Discovered by the Kotlin standard library dependency.
 *
 * @author Mikhail Polivakha
 */
public final class KotlinVersion extends LibraryComponent {

    public KotlinVersion() {
        super(
                "kotlin-stdlib",
                "org.jetbrains.kotlin",
                "Kotlin",
                "The version of Kotlin language being used in the application",
                true);
    }
}
