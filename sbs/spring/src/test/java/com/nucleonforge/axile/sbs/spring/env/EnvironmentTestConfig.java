package com.nucleonforge.axile.sbs.spring.env;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesConverter;
import com.nucleonforge.axile.sbs.spring.configprops.DefaultConfigurationPropertiesConverter;

/**
 * Environment test configuration.
 *
 * @author Mikhail Polivakha
 */
@TestConfiguration
public class EnvironmentTestConfig {

    @Bean
    public ConfigurationPropertiesConverter configurationPropertiesConverter() {
        return new DefaultConfigurationPropertiesConverter();
    }

    @Bean
    public ConfigurationPropertiesCache configurationPropertiesCache(
            ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint,
            ConfigurationPropertiesConverter configurationPropertiesConverter) {
        return new ConfigurationPropertiesCache(
                configurationPropertiesReportEndpoint, configurationPropertiesConverter);
    }

    @Bean
    public PropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultPropertyNameNormalizer();
    }

    @Bean
    public PropertyMetadataExtractor propertyMetadataExtractor(
            ConfigurableEnvironment environment, PropertyNameNormalizer propertyNameNormalizer) {
        return new DefaultPropertyMetadataExtractor(environment, propertyNameNormalizer);
    }

    @Bean
    public EnvPropertyEnricher envPropertyEnricher(
            Environment environment,
            DefaultPropertyNameNormalizer propertyNameNormalizer,
            ObjectProvider<ConfigurationPropertiesCache> configurationPropertiesCache,
            PropertyMetadataExtractor propertyMetadataExtractor) {
        return new DefaultEnvPropertyEnricher(
                environment, propertyNameNormalizer, configurationPropertiesCache, propertyMetadataExtractor);
    }
}
