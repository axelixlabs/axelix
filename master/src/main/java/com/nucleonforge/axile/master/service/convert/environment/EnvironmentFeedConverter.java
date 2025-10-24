package com.nucleonforge.axile.master.service.convert.environment;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.PropertySource;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse.PropertySourceShortProfile;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse.PropertySourceShortProfile.PropertyEntry;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link EnvironmentFeed} to {@link EnvironmentFeedResponse}.
 *
 * @since 27.08.2025
 * @author Nikita Kirillov
 */
@Service
public class EnvironmentFeedConverter implements Converter<EnvironmentFeed, EnvironmentFeedResponse> {

    @Override
    public @NonNull EnvironmentFeedResponse convertInternal(@NonNull EnvironmentFeed source) {
        List<String> activeProfiles = source.activeProfiles();
        List<String> defaultProfiles = source.defaultProfiles();
        List<PropertySourceShortProfile> propertySources = new ArrayList<>();

        for (PropertySource propertySource : source.propertySources()) {
            List<PropertyEntry> properties = getPropertyEntries(propertySource);
            propertySources.add(new PropertySourceShortProfile(propertySource.sourceName(), properties));
        }

        return new EnvironmentFeedResponse(activeProfiles, defaultProfiles, propertySources);
    }

    private List<PropertyEntry> getPropertyEntries(PropertySource propertySource) {
        List<PropertyEntry> properties = new ArrayList<>();

        if (propertySource.properties() != null) {
            for (var entry : propertySource.properties().entrySet()) {
                properties.add(new PropertyEntry(
                        entry.getKey(),
                        entry.getValue().value(),
                        entry.getValue().isPrimary()));
            }
        }
        return properties;
    }
}
