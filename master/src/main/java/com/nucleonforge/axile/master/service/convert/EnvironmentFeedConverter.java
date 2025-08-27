package com.nucleonforge.axile.master.service.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse;

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
            Map<String, String> properties = new HashMap<>();
            if (ps.properties() != null) {
                ps.properties().forEach((key, value) -> properties.put(key, value.value()));
            }
            propertySources.add(new EnvironmentFeedResponse.PropertySourceShortProfile(ps.sourceName(), properties));
        }

        return new EnvironmentFeedResponse(activeProfiles, defaultProfiles, propertySources);
    }
}
