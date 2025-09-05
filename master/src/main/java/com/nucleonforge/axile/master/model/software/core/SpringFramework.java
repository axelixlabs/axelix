package com.nucleonforge.axile.master.model.software.core;

import com.nucleonforge.axile.master.model.software.LibraryComponent;

/**
 * The Spring Framework version.
 *
 * @author Mikhail Polivakha
 */
public final class SpringFramework extends LibraryComponent {

    public SpringFramework() {
        super(
                "spring-core",
                "org.springframework",
                "Spring Framework",
                "The version of the Spring Framework being used in the application",
                true);
    }
}
