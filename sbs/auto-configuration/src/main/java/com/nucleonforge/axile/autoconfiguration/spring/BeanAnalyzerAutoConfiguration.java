package com.nucleonforge.axile.autoconfiguration.spring;

import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.spring.beans.BeanAnalyzer;
import com.nucleonforge.axile.spring.beans.BeansEndpointExtension;
import com.nucleonforge.axile.spring.beans.DefaultBeanAnalyzer;

/**
 * {@code BeanAnalyzerAutoConfiguration} auto-configuration class for {@link BeanAnalyzer} bean.
 *
 * <p>Provides a default {@link DefaultBeanAnalyzer} bean if no other
 * {@link BeanAnalyzer} bean is already defined in the Spring application context.</p>
 *
 * <p>Intended for automatic registration of the {@link BeanAnalyzer} implementation
 * to simplify configuration in Spring Boot applications.</p>
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
}
