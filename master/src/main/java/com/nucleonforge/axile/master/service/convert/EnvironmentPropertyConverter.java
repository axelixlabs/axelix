package com.nucleonforge.axile.master.service.convert;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.env.EnvironmentProperty;
import com.nucleonforge.axile.master.api.response.EnvironmentPropertyResponse;

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
