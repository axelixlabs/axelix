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
package com.nucleonforge.axelix.sbs.autoconfiguration.spring;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.sbs.spring.beans.AxelixBeansEndpoint;
import com.nucleonforge.axelix.sbs.spring.beans.BeanMetaInfoExtractor;
import com.nucleonforge.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractor;
import com.nucleonforge.axelix.sbs.spring.beans.QualifiersPersistencePostProcessor;
import com.nucleonforge.axelix.sbs.spring.conditions.ConditionalBeanRefBuilder;
import com.nucleonforge.axelix.sbs.spring.conditions.DefaultConditionalBeanRefBuilder;

/**
 * {@code BeanAnalyzerAutoConfiguration} auto-configuration class for {@link BeanMetaInfoExtractor} bean.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 * @author Sergey  Cherkasov
 */
@AutoConfiguration(after = BeansEndpointAutoConfiguration.class)
@ConditionalOnAvailableEndpoint(endpoint = BeansEndpoint.class)
public class BeanAnalyzerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConditionalBeanRefBuilder beanNameNormalizer() {
        return new DefaultConditionalBeanRefBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanMetaInfoExtractor defaultBeanMetaInfoExtractor(
            ConfigurableListableBeanFactory beanFactory,
            ConditionsReportEndpoint delegateConditions,
            ConditionalBeanRefBuilder conditionalBeanRefBuilder) {
        return new DefaultBeanMetaInfoExtractor(beanFactory, delegateConditions, conditionalBeanRefBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixBeansEndpoint beansEndpointExtension(
            BeansEndpoint beansEndpoint,
            BeanMetaInfoExtractor beanMetaInfoExtractor,
            ConfigurableApplicationContext context) {
        return new AxelixBeansEndpoint(beansEndpoint, beanMetaInfoExtractor, context);
    }

    @Bean
    public static QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
        return new QualifiersPersistencePostProcessor();
    }
}
