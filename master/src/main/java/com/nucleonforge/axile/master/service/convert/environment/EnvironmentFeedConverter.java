package com.nucleonforge.axile.master.service.convert.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.env.AxilePropertyValue;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse;
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
        List<EnvironmentFeedResponse.PropertySourceShortProfile> propertySources = new ArrayList<>();

        for (EnvironmentFeed.PropertySource ps : source.propertySources()) {
            List<EnvironmentFeedResponse.PropertySourceShortProfile.PropertyEntry> properties = getPropertyEntries(ps);
            propertySources.add(new EnvironmentFeedResponse.PropertySourceShortProfile(ps.sourceName(), properties));
        }

        return new EnvironmentFeedResponse(activeProfiles, defaultProfiles, propertySources);
    }

    private List<EnvironmentFeedResponse.PropertySourceShortProfile.PropertyEntry> getPropertyEntries(
            EnvironmentFeed.PropertySource propertySource) {
        List<EnvironmentFeedResponse.PropertySourceShortProfile.PropertyEntry> properties = new ArrayList<>();
        if (propertySource.properties() != null) {
            for (Map.Entry<String, AxilePropertyValue> entry :
                    propertySource.properties().entrySet()) {
                properties.add(new EnvironmentFeedResponse.PropertySourceShortProfile.PropertyEntry(
                        entry.getKey(),
                        entry.getValue().value(),
                        entry.getValue().isPrimary()));
            }
        }
        return properties;
    }
}
