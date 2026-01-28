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
package com.axelixlabs.axelix.sbs.spring.profiles;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Test-only configuration, implementations
 * used in profile-based tests of dynamic context mutation.
 *
 * <p>Each nested configuration class is annotated with {@link Profile},
 * and defines a single {@link FeatureService} bean with a specific name.</p>
 *
 * <p>This structure allows simulation of different runtime environments
 * by activating corresponding Spring profiles, e.g., {@code profile-basic},
 * {@code profile-premium}, etc.</p>
 *
 * <p>Intended to be imported explicitly into integration tests via {@code @Import}.</p>
 *
 * @since 11.07.2025
 * @author Nikita Kirillov
 */
public class TestFeatureServiceConfigs {

    public interface FeatureService {}

    public static class BasicFeatureService implements FeatureService {}

    public static class PremiumFeatureService implements FeatureService {}

    public static class AdvancedFeatureService implements FeatureService {}

    public static class LegacyFeatureService implements FeatureService {}

    @Configuration
    @Profile("profile-basic")
    public static class BasicFeatureServiceConfig {
        @Bean("basicFeatureService")
        public FeatureService basicFeatureService() {
            return new BasicFeatureService();
        }
    }

    @Configuration
    @Profile("profile-premium")
    public static class PremiumFeatureServiceConfig {
        @Bean("premiumFeatureService")
        public FeatureService premiumFeatureService() {
            return new PremiumFeatureService();
        }
    }

    @Configuration
    @Profile("profile-advanced")
    public static class AdvancedFeatureServiceConfig {
        @Bean("advancedFeatureService")
        public FeatureService advancedFeatureService() {
            return new AdvancedFeatureService();
        }
    }

    @Configuration
    @Profile("profile-legacy")
    public static class LegacyFeatureServiceConfig {
        @Bean("legacyFeatureService")
        public FeatureService legacyFeatureService() {
            return new LegacyFeatureService();
        }
    }
}
