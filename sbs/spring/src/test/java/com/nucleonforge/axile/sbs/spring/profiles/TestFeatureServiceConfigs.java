package com.nucleonforge.axile.sbs.spring.profiles;

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
