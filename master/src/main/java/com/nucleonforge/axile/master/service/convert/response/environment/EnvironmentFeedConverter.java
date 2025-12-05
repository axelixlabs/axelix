package com.nucleonforge.axile.master.service.convert.response.environment;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.PropertySource;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse.PropertyEntry;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse.PropertySourceShortProfile;
import com.nucleonforge.axile.master.service.convert.response.Converter;

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
            List<PropertyEntry> properties = convertPropertyEntries(propertySource);
            propertySources.add(new PropertySourceShortProfile(propertySource.sourceName(), properties));
        }

        return new EnvironmentFeedResponse(activeProfiles, defaultProfiles, propertySources);
    }

    private List<PropertyEntry> convertPropertyEntries(PropertySource propertySource) {
        List<PropertyEntry> properties = new ArrayList<>();

        if (propertySource.properties() != null) {
            for (EnvironmentFeed.Property property : propertySource.properties()) {
                properties.add(new PropertyEntry(
                        property.propertyName(),
                        property.value(),
                        property.isPrimary(),
                        property.configPropsBeanName(),
                        property.description(),
                        property.deprecated(),
                        property.deprecatedReason(),
                        property.deprecatedReplacement()));
            }
        }
        return properties;
    }
}
