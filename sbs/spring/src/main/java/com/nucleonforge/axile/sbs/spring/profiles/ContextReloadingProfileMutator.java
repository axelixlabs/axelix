/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
