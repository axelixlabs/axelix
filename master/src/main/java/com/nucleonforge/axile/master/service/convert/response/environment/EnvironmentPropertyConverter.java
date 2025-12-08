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

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.env.EnvironmentProperty;
import com.nucleonforge.axile.master.api.response.EnvironmentPropertyResponse;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link EnvironmentProperty} to {@link EnvironmentPropertyResponse}.
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
@Service
public class EnvironmentPropertyConverter implements Converter<EnvironmentProperty, EnvironmentPropertyResponse> {

    @Override
    public @NonNull EnvironmentPropertyResponse convertInternal(@NonNull EnvironmentProperty environmentProperty) {

        String propertySource = environmentProperty.property().source();
        String propertyValue = environmentProperty.property().value();
        List<EnvironmentPropertyResponse.PropertySource> responseSources = new ArrayList<>();

        for (EnvironmentProperty.SourceEntry entry : environmentProperty.propertySources()) {
            if (entry.property() != null) {
                EnvironmentPropertyResponse.Property property = new EnvironmentPropertyResponse.Property(
                        entry.property().value(), entry.property().origin());
                responseSources.add(new EnvironmentPropertyResponse.PropertySource(entry.sourceName(), property));
            }
        }

        return new EnvironmentPropertyResponse(propertySource, propertyValue, responseSources);
    }
}
