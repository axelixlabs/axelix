package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.nucleonforge.axile.sbs.spring.context.ContextRestarter;
import com.nucleonforge.axile.sbs.spring.env.DefaultEnvironmentPropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.env.EnvironmentPropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.properties.ContextReloadingPropertyMutator;
import com.nucleonforge.axile.sbs.spring.properties.DefaultPropertyDiscoverer;
import com.nucleonforge.axile.sbs.spring.properties.DefaultPropertySourceDescriber;
import com.nucleonforge.axile.sbs.spring.properties.PropertyDiscoverer;
import com.nucleonforge.axile.sbs.spring.properties.PropertyManagementEndpoint;
import com.nucleonforge.axile.sbs.spring.properties.PropertyMutator;
import com.nucleonforge.axile.sbs.spring.properties.PropertySourceDescriber;

/**
 * Auto-configuration for property management operations via Spring Boot Actuator.
 *
 * <p>This configuration provides beans to discover, describe, and mutate application properties at runtime,
 * as well as an actuator endpoint to expose these capabilities.</p>
 *
 * <p>Beans registered by this auto-configuration (if missing) include:</p>
 * <ul>
 *   <li>{@link PropertyMutator} — responsible for updating property values and triggering application context restarts.</li>
 *   <li>{@link PropertyDiscoverer} — responsible for locating properties within the Spring Environment.</li>
 *   <li>{@link PropertyManagementEndpoint} — actuator endpoint exposing property management operations.</li>
 *   <li>{@link PropertySourceDescriber} - Provides metadata descriptions of property sources.</li>
 * </ul>
 *
 * <p>This auto-configuration is applied after {@link ContextRestarterAutoConfiguration}
 * to ensure context restart capabilities are available when mutating properties.</p>
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(after = ContextRestarterAutoConfiguration.class)
public class PropertyManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PropertyMutator propertyMutator(ConfigurableEnvironment environment, ContextRestarter contextRestarter) {
        return new ContextReloadingPropertyMutator(environment, contextRestarter);
    }

    @Bean
    @ConditionalOnMissingBean
    public EnvironmentPropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultEnvironmentPropertyNameNormalizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyDiscoverer propertyDiscoverer(
            Environment environment, EnvironmentPropertyNameNormalizer propertyNameNormalizer) {
        return new DefaultPropertyDiscoverer(environment, propertyNameNormalizer);
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyManagementEndpoint propertyManagementEndpoint(
            PropertyMutator propertyMutator, PropertyDiscoverer propertyDiscoverer) {
        return new PropertyManagementEndpoint(propertyMutator, propertyDiscoverer);
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertySourceDescriber propertySourceDescriber() {
        return new DefaultPropertySourceDescriber();
    }
}
