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
package com.nucleonforge.axile.sbs.spring.cache;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.CacheManager;

/**
 * BeanPostProcessor that wraps existing CacheManager beans with EnhancedCacheManager
 * to provide additional features.
 *
 * @since 24.11.2025
 * @author Nikita Kirillov
 */
public class CacheManagerBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof CacheManager && !(bean instanceof EnhancedCacheManager)) {
            return new EnhancedCacheManager((CacheManager) bean);
        }
        return bean;
    }
}
