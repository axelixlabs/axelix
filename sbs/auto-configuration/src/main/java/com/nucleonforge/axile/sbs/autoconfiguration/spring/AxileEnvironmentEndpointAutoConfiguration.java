package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.nucleonforge.axile.sbs.spring.env.AxileEnvironmentEndpoint;
import com.nucleonforge.axile.sbs.spring.env.DefaultEnvPropertyEnricher;
import com.nucleonforge.axile.sbs.spring.env.EnvPropertyEnricher;
import com.nucleonforge.axile.sbs.spring.properties.utils.EnvironmentPropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.properties.utils.InvalidPropertiesLoader;

/**
 * Auto-configuration for the {@link AxileEnvironmentEndpoint}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(
        after = {
            EnvironmentEndpointAutoConfiguration.class,
            AxileConfigurationsPropertiesEndpointAutoConfiguration.class,
            PropertyUtilsAutoConfiguration.class
        })
@ConditionalOnAvailableEndpoint(endpoint = EnvironmentEndpoint.class)
public class AxileEnvironmentEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EnvPropertyEnricher envPropertyEnricher(
            Environment environment,
            EnvironmentPropertyNameNormalizer propertyNameNormalizer,
            ObjectProvider<ConfigurationPropertiesCache> cache,
            InvalidPropertiesLoader invalidPropertiesLoader) {
        return new DefaultEnvPropertyEnricher(environment, propertyNameNormalizer, cache, invalidPropertiesLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxileEnvironmentEndpoint axileEnvironmentEndpoint(
            EnvironmentEndpoint environmentEndpoint, EnvPropertyEnricher envPropertyEnricher) {
        return new AxileEnvironmentEndpoint(environmentEndpoint, envPropertyEnricher);
    }
}
