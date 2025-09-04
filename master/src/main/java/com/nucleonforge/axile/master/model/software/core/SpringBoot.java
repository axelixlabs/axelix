package com.nucleonforge.axile.master.model.software.core;

import com.nucleonforge.axile.master.model.software.LibraryComponent;

/**
 * The Spring Boot library.
 *
 * @author Mikhail Polivakha
 */
public final class SpringBoot extends LibraryComponent {

    public SpringBoot() {
        super(
                "spring-boot",
                "org.springframework.boot",
                "Spring Boot",
                "The version of the Spring Boot being used in the application",
                true);
    }
}
