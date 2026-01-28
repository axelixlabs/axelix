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
package com.axelixlabs.axelix.sbs.spring.conditions;

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
