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
 * Auto-configuration class for {@link ActuatorEndpoints#BEANS} endpoint.
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
