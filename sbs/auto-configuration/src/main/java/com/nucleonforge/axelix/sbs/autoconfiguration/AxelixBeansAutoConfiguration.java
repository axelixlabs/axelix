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
package com.nucleonforge.axelix.sbs.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axelix.sbs.spring.beans.AxelixBeansEndpoint;
import com.nucleonforge.axelix.sbs.spring.beans.BeanMetaInfoExtractor;
import com.nucleonforge.axelix.sbs.spring.beans.BeansFeedBuilder;
import com.nucleonforge.axelix.sbs.spring.beans.CachingBeansFeedBuilder;
import com.nucleonforge.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractor;
import com.nucleonforge.axelix.sbs.spring.beans.DefaultBeansFeedBuilder;
import com.nucleonforge.axelix.sbs.spring.beans.QualifiersPersistencePostProcessor;
import com.nucleonforge.axelix.sbs.spring.conditions.ConditionalBeanRefBuilder;
import com.nucleonforge.axelix.sbs.spring.conditions.DefaultConditionalBeanRefBuilder;

/**
 * Auto-configuration class for {@link ActuatorEndpoints#GET_BEANS} endpoint.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class AxelixBeansAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConditionalBeanRefBuilder defaultConditionalBeanRefBuilder() {
        return new DefaultConditionalBeanRefBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanMetaInfoExtractor defaultBeanMetaInfoExtractor(
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
    @Primary
    public BeansFeedBuilder cachingBeansFeedBuilder(BeansFeedBuilder defaultBeansFeedBuilder) {
        return new CachingBeansFeedBuilder(defaultBeansFeedBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixBeansEndpoint beansEndpointExtension(BeansFeedBuilder cachingBeansFeedBuilder) {
        return new AxelixBeansEndpoint(cachingBeansFeedBuilder);
    }

    @Bean
    public static QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
        return new QualifiersPersistencePostProcessor();
    }
}
