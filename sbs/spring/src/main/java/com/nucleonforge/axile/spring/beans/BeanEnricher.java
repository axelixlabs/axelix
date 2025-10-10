package com.nucleonforge.axile.spring.beans;

import com.nucleonforge.axile.common.api.BeansFeed;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * Enriches standard bean descriptor with additional analysis information.
 *
 * @since 04.07.2025
 * @author Nikita Kirillov
 */
public interface BeanEnricher {

    /**
     * Enriches bean descriptor with additional analysis information.
     *
     * @param beanName the name of the bean to analyze
     * @param beanDescriptor the standard bean descriptor from Actuator
     * @param context the application context where the bean is defined
     * @return enriched bean information or empty if bean not found
     */
    Optional<BeansFeed.Bean> enrich(String beanName, BeansEndpoint.BeanDescriptor beanDescriptor, ApplicationContext context);
}
