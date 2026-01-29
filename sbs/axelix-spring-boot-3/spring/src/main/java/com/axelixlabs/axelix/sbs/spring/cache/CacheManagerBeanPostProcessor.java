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
package com.axelixlabs.axelix.sbs.spring.cache;

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
