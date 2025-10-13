package com.nucleonforge.axile.autoconfiguration.spring;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.spring.beans.BeanMetaInfoExtractor;
import com.nucleonforge.axile.spring.beans.BeansEndpointExtension;
import com.nucleonforge.axile.spring.beans.DefaultBeanMetaInfoExtractor;
import com.nucleonforge.axile.spring.beans.QualifiersPersistencePostProcessor;

/**
 * {@code BeanAnalyzerAutoConfiguration} auto-configuration class for {@link BeanMetaInfoExtractor} bean.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = BeansEndpoint.class)
public class BeanAnalyzerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BeanMetaInfoExtractor defaultBeanMetaInfoExtractor(ConfigurableListableBeanFactory beanFactory) {
        return new DefaultBeanMetaInfoExtractor(beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public BeansEndpointExtension beansEndpointExtension(
            BeansEndpoint beansEndpoint,
            BeanMetaInfoExtractor beanMetaInfoExtractor,
            ConfigurableApplicationContext context) {
        return new BeansEndpointExtension(beansEndpoint, beanMetaInfoExtractor, context);
    }

    @Bean
    @ConditionalOnMissingBean
    public QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
        return new QualifiersPersistencePostProcessor();
    }
}
