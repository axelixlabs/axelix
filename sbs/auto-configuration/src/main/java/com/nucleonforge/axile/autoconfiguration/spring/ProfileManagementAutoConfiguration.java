package com.nucleonforge.axile.autoconfiguration.spring;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.nucleonforge.axile.spring.context.ContextRestarter;
import com.nucleonforge.axile.spring.profiles.ContextReloadingProfileMutator;
import com.nucleonforge.axile.spring.profiles.ProfileManagementEndpoint;
import com.nucleonforge.axile.spring.profiles.ProfileMutator;

/**
 * Auto-configuration for profile management operations via Spring Boot Actuator.
 *
 * <p>This configuration provides beans to mutate active application profiles at runtime,
 * as well as an actuator endpoint to expose these capabilities.</p>
 *
 * <p>Beans registered by this auto-configuration (if missing) include:</p>
 * <ul>
 *   <li>{@link ProfileMutator} — responsible for activating, replacing, and deactivating profiles with context restarts.</li>
 *   <li>{@link ProfileManagementEndpoint} — actuator endpoint exposing profile management operations.</li>
 * </ul>
 *
 * <p>This auto-configuration is applied after {@link ContextRestarterAutoConfiguration}
 * to ensure context restart capabilities are available when mutating profiles.</p>
 *
 * @since 11.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(after = ContextRestarterAutoConfiguration.class)
@ConditionalOnAvailableEndpoint(endpoint = ProfileManagementEndpoint.class)
public class ProfileManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProfileMutator profileMutator(ConfigurableEnvironment environment, ContextRestarter contextRestarter) {
        return new ContextReloadingProfileMutator(environment, contextRestarter);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProfileManagementEndpoint profileManagementEndpoint(ProfileMutator profileMutator) {
        return new ProfileManagementEndpoint(profileMutator);
    }
}
