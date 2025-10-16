package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.conditions.ConditionsReportEndpointExtension;

/**
 * Auto-configuration for the {@link ConditionsReportEndpointExtension}.
 *
 * @since 20.10.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(after = ConditionsReportEndpointAutoConfiguration.class)
@ConditionalOnAvailableEndpoint(endpoint = ConditionsReportEndpoint.class)
public class ConditionsReportEndpointExtensionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConditionsReportEndpointExtension conditionsReportEndpointExtension(
            ConditionsReportEndpoint conditionsReportEndpoint) {
        return new ConditionsReportEndpointExtension(conditionsReportEndpoint);
    }
}
