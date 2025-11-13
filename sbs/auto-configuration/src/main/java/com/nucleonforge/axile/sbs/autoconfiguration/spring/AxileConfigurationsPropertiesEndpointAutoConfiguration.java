package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.boot.actuate.autoconfigure.context.properties.ConfigurationPropertiesReportEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.configprops.AxileConfigurationPropertiesEndpoint;
import com.nucleonforge.axile.sbs.spring.configprops.ServiceConfigurationProperties;

/**
 * Auto-configuration for the {@link AxileConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@AutoConfiguration(after = ConfigurationPropertiesReportEndpointAutoConfiguration.class)
@ConditionalOnAvailableEndpoint(endpoint = ConfigurationPropertiesReportEndpoint.class)
public class AxileConfigurationsPropertiesEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceConfigurationProperties serviceConfigurationProperties(
            ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint) {
        return new ServiceConfigurationProperties(configurationPropertiesReportEndpoint);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxileConfigurationPropertiesEndpoint axileConfigurationPropertiesEndpoint(
            ServiceConfigurationProperties serviceConfigurationProperties) {
        return new AxileConfigurationPropertiesEndpoint(serviceConfigurationProperties);
    }
}
