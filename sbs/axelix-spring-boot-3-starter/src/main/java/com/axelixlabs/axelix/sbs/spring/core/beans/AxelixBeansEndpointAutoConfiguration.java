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
package com.axelixlabs.axelix.sbs.spring.core.beans;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.conditions.ConditionalBeanRefBuilder;
import com.axelixlabs.axelix.sbs.spring.core.conditions.DefaultConditionalBeanRefBuilder;

/**
 * Auto-configuration class for the beans custom actuator endpoint.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@AutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = AxelixBeansEndpoint.class)
public class AxelixBeansEndpointAutoConfiguration {

    @Bean
    public ConditionalBeanRefBuilder conditionalBeanRefBuilder() {
        return new DefaultConditionalBeanRefBuilder();
    }

    @Bean
    public BeanMetaInfoExtractor beanMetaInfoExtractor(
            ConfigurableApplicationContext configurableApplicationContext,
            ConditionalBeanRefBuilder conditionalBeanRefBuilder) {
        return new DefaultBeanMetaInfoExtractor(configurableApplicationContext, conditionalBeanRefBuilder);
    }

    @Bean
    public BeansFeedBuilder defaultBeansFeedBuilder(
            BeanMetaInfoExtractor beanMetaInfoExtractor, ConfigurableApplicationContext context) {
        return new DefaultBeansFeedBuilder(beanMetaInfoExtractor, context);
    }

    @Bean
    public AxelixBeansEndpoint axelixBeansEndpoint(BeansFeedBuilder cachingBeansFeedBuilder) {
        return new AxelixBeansEndpoint(cachingBeansFeedBuilder);
    }

    @Bean
    public static QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
        return new QualifiersPersistencePostProcessor();
    }
}
