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

import java.util.function.Supplier;

import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.sbs.spring.core.conditions.ConditionalBeanRefBuilder;
import com.axelixlabs.axelix.sbs.spring.core.conditions.DefaultConditionalBeanRefBuilder;

/**
 * Test configuration for {@link AxelixBeansEndpointTest}, part of the shared endpoint test context.
 *
 * @author Mikhail Polivakha
 */
@TestConfiguration
@Import({BeansEndpoint.class, AxelixBeansEndpoint.class, ConditionsReportEndpoint.class})
public class BeansEndpointTestConfiguration {

    public static final String QUALIFIERS_PERSISTENCE_POST_PROCESSOR = "qualifiersPersistencePostProcessor";
    public static final String BEAN_META_INFO_EXTRACTOR = "beanMetaInfoExtractor";
    public static final String CUSTOM_SUPPLIER = "customSupplier";

    @Bean
    public ConditionalBeanRefBuilder conditionalBeanRefBuilder() {
        return new DefaultConditionalBeanRefBuilder();
    }

    @Bean
    public BeansFeedBuilder testBeansFeedBuilder(
            BeanMetaInfoExtractor beanMetaInfoExtractor,
            ConfigurableApplicationContext configurableApplicationContext) {
        return new DefaultBeansFeedBuilder(beanMetaInfoExtractor, configurableApplicationContext);
    }

    @Bean(BEAN_META_INFO_EXTRACTOR)
    public BeanMetaInfoExtractor beanMetaInfoExtractor(
            ConfigurableApplicationContext configurableApplicationContext,
            ConditionalBeanRefBuilder conditionalBeanRefBuilder) {
        return new DefaultBeanMetaInfoExtractor(configurableApplicationContext, conditionalBeanRefBuilder);
    }

    @Bean(QUALIFIERS_PERSISTENCE_POST_PROCESSOR)
    public static QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
        return new QualifiersPersistencePostProcessor();
    }

    @Bean(CUSTOM_SUPPLIER)
    public Supplier<String> customSupplier() {
        return () -> "value";
    }
}
