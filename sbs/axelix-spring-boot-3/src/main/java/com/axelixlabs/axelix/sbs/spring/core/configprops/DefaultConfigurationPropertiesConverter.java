/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed.ConfigurationProperties;
import com.axelixlabs.axelix.common.utils.BeanNameUtils;

/**
 * {@link ConfigurationPropertiesConverter} that flattens out the configuration properties feed as it is returned from the
 * Spring Boot native actuator endpoint.
 *
 * @author Sergey Cherkasov
 */
public class DefaultConfigurationPropertiesConverter implements ConfigurationPropertiesConverter {

    private final ConfigurationPropertiesFlattener configPropsFlattener;

    public DefaultConfigurationPropertiesConverter(ConfigurationPropertiesFlattener configPropsFlattener) {
        this.configPropsFlattener = configPropsFlattener;
    }

    @NonNull
    @Override
    public ConfigurationPropertiesFeed convert(ConfigurationPropertiesDescriptor originalDescriptor) {
        List<ConfigurationProperties> configurationProperties = new ArrayList<>();

        originalDescriptor.getContexts().values().forEach(context -> {
            if (context != null && context.getBeans() != null) {
                context.getBeans()
                        .forEach((beanName, bean) -> configurationProperties.add(new ConfigurationProperties(
                                BeanNameUtils.stripConfigPropsPrefix(beanName),
                                bean.getPrefix(),
                                configPropsFlattener.flatten(bean.getProperties()),
                                configPropsFlattener.flatten(bean.getInputs()))));
            }
        });

        return new ConfigurationPropertiesFeed(configurationProperties);
    }
}
