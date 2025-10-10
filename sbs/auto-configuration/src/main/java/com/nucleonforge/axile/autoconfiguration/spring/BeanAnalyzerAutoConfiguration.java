package com.nucleonforge.axile.autoconfiguration.spring;

import com.nucleonforge.axile.spring.beans.CachingBeanEnricher;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.spring.beans.BeanEnricher;
import com.nucleonforge.axile.spring.beans.BeansEndpointExtension;
import com.nucleonforge.axile.spring.beans.DefaultBeanEnricher;
import com.nucleonforge.axile.spring.beans.QualifiersPersistencePostProcessor;

/**
 * {@code BeanAnalyzerAutoConfiguration} auto-configuration class for {@link BeanEnricher} bean.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class BeanAnalyzerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BeanEnricher beanAnalyzer() {
        return new CachingBeanEnricher(new DefaultBeanEnricher());
    }

    @Bean
    @ConditionalOnMissingBean
    public BeansEndpointExtension beansEndpointExtension(BeansEndpoint beansEndpoint, BeanEnricher beanEnricher, ApplicationContext context) {
        return new BeansEndpointExtension(beansEndpoint, beanEnricher, context);
    }

    @Bean
    @ConditionalOnMissingBean
    public QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
        return new QualifiersPersistencePostProcessor();
    }
}
