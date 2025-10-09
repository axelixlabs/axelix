package com.nucleonforge.axile.spring.beans;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Response wrapper containing metadata about a Spring bean definition.
 *
 * @param isLazyInit indicates if the bean is configured for lazy initialization
 * @param isPrimary indicates if the bean is marked as primary candidate for autowiring
 * @param qualifiers list of qualifier annotations associated with the bean
 * @param enclosingClassName the name of the class that defines this bean (configuration class for @Bean methods,
 *                          bean class for component-scanned beans, factory bean class for factory-created beans)
 * @param methodName the name of the method that created the bean (@Bean methods or factory methods)
 * @param factoryBeanName the name of the factory bean that produced this bean
 *
 * @since 04.07.2025
 * @author Nikita Kirillov
 */
public record BeanProfile(
        boolean isLazyInit,
        boolean isPrimary,
        List<String> qualifiers,
        @Nullable String enclosingClassName,
        @Nullable String methodName,
        @Nullable String factoryBeanName) {}
