/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.sbs.spring.beans;

import org.jspecify.annotations.NullMarked;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Interface capable to extract the {@link BeanMetaInfo} from the given {@link ConfigurableListableBeanFactory}.
 * by the given bean name.
 *
 * @since 04.07.2025
 * @author Nikita Kirillov
 */
@NullMarked
public interface BeanMetaInfoExtractor {

    /**
     * Enriches bean descriptor with additional analysis information.
     *
     * @param beanName    the name of the bean to analyze
     * @param beanFactory the bean factory that stores the bean with the given {@code beanName}
     * @return enriched bean information or empty if bean not found
     */
    BeanMetaInfo extract(String beanName, ConfigurableListableBeanFactory beanFactory);
}
