package com.nucleonforge.axile.sbs.spring.env;

/**
 * Interface capable to normalize the property name according to Spring Boot's relaxed binding rules.
 *
 * @apiNote <a href="https://github.com/spring-projects/spring-boot/wiki/relaxed-binding-2.0">Relaxed Binding doc</a>
 * @author Mikhail Polivakha
 */
public interface EnvironmentPropertyNameNormalizer {

    /**
     * @param propertyName inbound property name, to be normalized
     * @return normalized property name
     */
    String normalize(String propertyName);
}
