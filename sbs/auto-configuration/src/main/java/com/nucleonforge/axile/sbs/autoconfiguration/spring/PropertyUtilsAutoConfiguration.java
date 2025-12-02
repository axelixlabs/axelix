package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.properties.utils.DefaultEnvironmentPropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.properties.utils.EnvironmentPropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.properties.utils.InvalidPropertiesLoader;
import com.nucleonforge.axile.sbs.spring.properties.utils.YamlInvalidPropertiesLoader;

/**
 * Auto-configuration for property-related utilities.
 *
 * @since 01.12.2025
 * @author Nikita Kirillov
 */
public class PropertyUtilsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EnvironmentPropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultEnvironmentPropertyNameNormalizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public InvalidPropertiesLoader invalidPropertiesLoader(EnvironmentPropertyNameNormalizer propertyNameNormalizer) {
        return new YamlInvalidPropertiesLoader(propertyNameNormalizer);
    }
}
