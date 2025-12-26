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
package com.nucleonforge.axile.sbs.spring.conditions;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;

/**
 * Interface capable to convert the bean class and optional factory method to the
 * bean reference representation used in the {@link ConditionsReportEndpoint}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public interface ConditionalBeanRefBuilder {

    default @Nullable String buildBeanRef(@Nullable Class<?> beanClass, @Nullable String beanFactoryMethodName) {
        return beanClass != null ? buildBeanRefInternal(beanClass, beanFactoryMethodName) : null;
    }

    /**
     * Examples:
     * <pre>
     * Input: WebMvcAutoConfiguration$EnableWebMvcConfiguration, null
     * Output: WebMvcAutoConfiguration.EnableWebMvcConfiguration
     *
     * Input: SecurityAutoConfiguration$JwtAutoConfiguration, jwtProperties
     * Output: WebMvcAutoConfiguration.EnableWebMvcConfiguration#jwtProperties
     * </pre>
     *
     * @param beanClass the bean's class. Can be a proxy class.
     *                  Implementations must be capable to resolve the end-user class.
     * @param beanFactoryMethodName the name of the factory method that produced bean.
     *                              Usually applicable only for {@link org.springframework.context.annotation.Bean @Bean} methods
     * @return adapted conditions name
     */
    String buildBeanRefInternal(Class<?> beanClass, @Nullable String beanFactoryMethodName);
}
