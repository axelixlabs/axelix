package com.nucleonforge.axile.master.api.response;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;

/**
 * The feed of the environment used in the application.
 *
 * @param activeProfiles   the list of currently active Spring profiles
 * @param defaultProfiles  the list of default Spring profiles
 * @param propertySources  the list of property sources with their short profiles
 *
 * @see EnvironmentFeed
 * @since 27.08.2025
 * @author Nikita Kirillov
 */
public record EnvironmentFeedResponse(
        List<String> activeProfiles, List<String> defaultProfiles, List<PropertySourceShortProfile> propertySources) {

    /**
     * Short profile of a given property source.
     *
     * @param name       the sourceName of the property source
     * @param properties the list of property entries with name, value and primary flag
     */
    public record PropertySourceShortProfile(String name, List<PropertyEntry> properties) {

        /**
         * Represents a property with its value and whether it is the primary ("winning") property.
         *
         * @param name       the property name
         * @param value     the property value
         * @param isPrimary whether this property value is primary (i.e. this value takes precedence over
         *                  the other values from other property sources)
         */
        public record PropertyEntry(String name, @Nullable String value, boolean isPrimary) {}
    }
}
