/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.axelixlabs.axelix.sbs.spring.context.ContextRestarter;
import com.axelixlabs.axelix.sbs.spring.profiles.ContextReloadingProfileMutator;
import com.axelixlabs.axelix.sbs.spring.profiles.ProfileManagementEndpoint;
import com.axelixlabs.axelix.sbs.spring.profiles.ProfileMutator;

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
