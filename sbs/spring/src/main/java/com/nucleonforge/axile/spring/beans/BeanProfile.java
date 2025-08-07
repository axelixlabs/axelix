package com.nucleonforge.axile.spring.beans;

import java.lang.reflect.Method;

/**
 * Response wrapper containing metadata about a Spring bean definition.
 *
 * @param beanName         the name of the bean in the context
 * @param beanClass        the resolved runtime class of the bean (may be a proxy class)
 * @param definingMethod   the method that defined the bean, if any (e.g., @Bean method or factory method)
 * @param scope            the scope of the bean (e.g., {@code singleton}, {@code prototype})
 * @param factoryBean      {@code true} if the bean was created via a factory bean, {@code false} otherwise
 *
 * @since 04.07.2025
 * @author Nikita Kirillov
 */
public record BeanProfile(
        String beanName, Class<?> beanClass, Method definingMethod, String scope, boolean factoryBean) {}
