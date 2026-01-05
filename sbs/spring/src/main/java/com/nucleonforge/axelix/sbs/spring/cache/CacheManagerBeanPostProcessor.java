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
package com.nucleonforge.axelix.sbs.spring.cache;

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

    // TODO:
    //  This is a dangerous practise.
    //  The fact is that if the end-users have stuff like "cacheManager instanceof Caffiene" or smth
    //  like that in their code, then our bean post processor will essentially break this code.
    //  The problem above can be solved by creating a CGLIB proxy in runtime. The question is - in this case,
    //  we would have to be sure that the concrete CacheManager class is not a final class, so we can create an
    //  decedent.
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof CacheManager && !(bean instanceof EnhancedCacheManager)) {
            return new EnhancedCacheManager(beanName, (CacheManager) bean);
        }
        return bean;
    }
}
