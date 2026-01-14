/*
 * Copyright 2025-present, Nucleon Forge Software.
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
package com.nucleonforge.axelix.sbs.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.nucleonforge.axelix.sbs.spring.context.ContextRestarter;
import com.nucleonforge.axelix.sbs.spring.profiles.ContextReloadingProfileMutator;
import com.nucleonforge.axelix.sbs.spring.profiles.ProfileManagementEndpoint;
import com.nucleonforge.axelix.sbs.spring.profiles.ProfileMutator;

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
