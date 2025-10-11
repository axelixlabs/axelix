package com.nucleonforge.axile.spring.beans;

import org.jspecify.annotations.NullMarked;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Interface capable to extract the {@link BeanMetaInfo} from the given {@link ConfigurableListableBeanFactory}.
 * by the given bean name.
 *
 * @since 04.07.2025
 * @author Nikita Kirillov
 */
@NullMarked
public interface BeanMetaInfoExtractor {

    /**
     * Enriches bean descriptor with additional analysis information.
     *
     * @param beanName    the name of the bean to analyze
     * @param beanFactory the bean factory that stores the bean with the given {@code beanName}
     * @return enriched bean information or empty if bean not found
     */
    BeanMetaInfo extract(String beanName, ConfigurableListableBeanFactory beanFactory);
}
