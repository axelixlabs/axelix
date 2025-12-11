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
package com.nucleonforge.axile.master.service.convert.response.environment;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
 * @author Sergey Cherkasov
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
            propertySources.add(new PropertySourceShortProfile(
                    propertySource.sourceName(), propertySource.sourceDescription(), properties));
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
                        mapDeprecation(property.deprecation())));
            }
        }

        return properties;
    }

    private EnvironmentFeedResponse.@Nullable Deprecation mapDeprecation(
            EnvironmentFeed.@Nullable Deprecation deprecation) {
        if (deprecation == null) {
            return null;
        }

        return new EnvironmentFeedResponse.Deprecation(deprecation.reason(), deprecation.replacement());
    }
}
