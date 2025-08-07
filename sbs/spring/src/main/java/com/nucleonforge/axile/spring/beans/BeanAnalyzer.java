package com.nucleonforge.axile.spring.beans;

import java.util.Optional;

/**
 * Contract for analyzing metadata of a Spring bean registered in the application context.
 *
 * @since 04.07.2025
 * @author Nikita Kirillov
 */
public interface BeanAnalyzer {

    /**
     * Analyzes the given bean by name and returns detailed metadata about it.
     *
     * @param beanName the name of the bean in the Spring application context
     * @return an {@link Optional} containing {@link BeanProfile} if the bean is found;
     *         otherwise, an empty Optional
     */
    Optional<BeanProfile> analyze(String beanName);
}
