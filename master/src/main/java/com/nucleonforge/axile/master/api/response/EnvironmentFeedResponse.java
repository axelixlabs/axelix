package com.nucleonforge.axile.master.api.response;

import java.util.List;
import java.util.Map;

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
 * author Nikita Kirillov
 */
public record EnvironmentFeedResponse(
        List<String> activeProfiles, List<String> defaultProfiles, List<PropertySourceShortProfile> propertySources) {

    /**
     * Short profile of a given property source.
     *
     * @param name       the sourceName of the property source
     * @param properties the map of property names to their string values
     */
    public record PropertySourceShortProfile(String name, Map<String, String> properties) {}
}
