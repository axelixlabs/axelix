package com.nucleonforge.axile.sbs.spring.profiles;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import com.nucleonforge.axile.sbs.spring.context.ContextRestarter;

/**
 * {@link ProfileMutator} implementation that updates the active Spring profiles
 * and restarts the {@link ApplicationContext} to propagate the changes throughout the application.
 *
 * @since 11.07.2025
 * @author Nikita Kirillov
 */
public class ContextReloadingProfileMutator implements ProfileMutator {

    private final ConfigurableEnvironment environment;
    private final ContextRestarter contextRestarter;

    public ContextReloadingProfileMutator(ConfigurableEnvironment environment, ContextRestarter contextRestarter) {
        this.environment = environment;
        this.contextRestarter = contextRestarter;
    }

    @Override
    public ProfileMutationResponse replaceActiveProfiles(String[] profiles) {
        environment.setActiveProfiles(profiles);

        contextRestarter.restartContext();

        return new ProfileMutationResponse(true, "New profiles have been activated");
    }
}
