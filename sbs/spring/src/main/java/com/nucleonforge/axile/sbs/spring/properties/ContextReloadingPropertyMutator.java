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
package com.nucleonforge.axile.sbs.spring.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.nucleonforge.axile.sbs.spring.context.ContextRestarter;

import static com.nucleonforge.axile.sbs.spring.properties.AxilePropertySource.AXILE_PROPERTY_SOURCE_NAME;

/**
 * {@link PropertyMutator} that reloads the {@link ApplicationContext} after property change so that the change
 * propagates everywhere where required.
 *
 * @since 07.04.2025
 * @author Mikhail Polivakha
 */
public class ContextReloadingPropertyMutator implements PropertyMutator {

    private final ConfigurableEnvironment configurableEnvironment;
    private final ContextRestarter contextRestarter;

    public ContextReloadingPropertyMutator(
            ConfigurableEnvironment configurableEnvironment, ContextRestarter contextRestarter) {
        this.configurableEnvironment = configurableEnvironment;
        this.contextRestarter = contextRestarter;
    }

    @Override
    public void mutate(String propertyName, String newValue) {
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        PropertySource<?> potentiallyAxilePropertySource = propertySources.get(AXILE_PROPERTY_SOURCE_NAME);

        if (potentiallyAxilePropertySource == null) {
            Map<String, Object> source = new HashMap<>();
            source.put(propertyName, newValue);
            propertySources.addFirst(new AxilePropertySource(source));
        } else {
            var axilePropertySource = (AxilePropertySource) potentiallyAxilePropertySource;
            axilePropertySource.addProperty(propertyName, newValue);
        }

        contextRestarter.restartContext();
    }
}
