package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import feign.Feign;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.integrations.IntegrationComponentDiscoverer;
import com.nucleonforge.axile.sbs.spring.integrations.http.FeignClientIntegrationDiscoverer;
import com.nucleonforge.axile.sbs.spring.integrations.http.HttpIntegration;

/**
 * Auto-configuration for discovering HTTP integrations based on Spring Cloud OpenFeign.
 * <p>
 * Registers a {@link FeignClientIntegrationDiscoverer} if Feign is present on the classpath.
 * </p>
 *
 * @author Nikita Kirillov
 * @since 09.07.2025
 */
@AutoConfiguration
@ConditionalOnClass({Feign.class, FeignClient.class})
public class SpringCloudFeignAutoConfiguration {

    @Bean
    public IntegrationComponentDiscoverer<HttpIntegration> feignClientIntegrationDiscoverer(
            ApplicationContext context) {
        return new FeignClientIntegrationDiscoverer(context);
    }
}
