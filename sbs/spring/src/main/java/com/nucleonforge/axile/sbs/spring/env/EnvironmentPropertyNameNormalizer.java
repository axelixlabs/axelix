package com.nucleonforge.axile.sbs.spring.env;

/**
 * Interface capable to "normalize" the property name. The normalization is the process that
 * converts a property from its specific form like {@code FOO_BAR} or {@code foo.bar[1]} to some
 * canonical view.
 * <p>
 * This is somewhat similar to relaxed binding in Spring Boot, however for various reasons we cannot
 * directly use the API of relaxed binding.
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
