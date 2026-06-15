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
package com.axelixlabs.axelix.sbs.spring.core.conditions;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration for {@link AxelixConditionsEndpointTest}, part of the shared endpoint test context.
 *
 * @author Sergey Cherkasov
 */
@TestConfiguration
public class ConditionsEndpointTestConfiguration {

    @Bean
    public ConditionalTargetUnwrapper conditionalNameUnwrap() {
        return new DefaultConditionalTargetUnwrapper();
    }

    @Bean
    public ConditionalFeedBuilder conditionalFeedBuilder(
            ConfigurableApplicationContext configurableApplicationContext,
            ConditionalTargetUnwrapper conditionalTargetUnwrapper) {
        return new DefaultConditionalFeedBuilder(configurableApplicationContext, conditionalTargetUnwrapper);
    }

    @Bean
    public AxelixConditionsEndpoint axelixConditionsEndpoint(ConditionalFeedBuilder conditionalFeedBuilder) {
        return new AxelixConditionsEndpoint(conditionalFeedBuilder);
    }

    @Bean
    @ConditionalOnProperty(name = "axelix.conditions.test.flag", havingValue = "enabled")
    public String positiveConditionBean() {
        return "positive";
    }

    @Bean
    @ConditionalOnProperty(name = "axelix.conditions.test.flag", havingValue = "disabled")
    public String negativeConditionBean() {
        return "negative";
    }
}
