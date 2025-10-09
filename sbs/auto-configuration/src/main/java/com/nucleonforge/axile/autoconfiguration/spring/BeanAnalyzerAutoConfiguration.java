package com.nucleonforge.axile.autoconfiguration.spring;

import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.spring.beans.BeanAnalyzer;
import com.nucleonforge.axile.spring.beans.BeansEndpointExtension;
import com.nucleonforge.axile.spring.beans.DefaultBeanAnalyzer;
import com.nucleonforge.axile.spring.beans.QualifiersPersistencePostProcessor;

/**
 * {@code BeanAnalyzerAutoConfiguration} auto-configuration class for {@link BeanAnalyzer} bean.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class BeanAnalyzerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BeanAnalyzer beanAnalyzer(ApplicationContext context) {
        return new DefaultBeanAnalyzer(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public BeansEndpointExtension beansEndpointExtension(BeansEndpoint beansEndpoint, BeanAnalyzer beanAnalyzer) {
        return new BeansEndpointExtension(beansEndpoint, beanAnalyzer);
    }

    @Bean
    @ConditionalOnMissingBean
    public QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
        return new QualifiersPersistencePostProcessor();
    }
}
