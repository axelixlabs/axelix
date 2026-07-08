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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import org.jspecify.annotations.NonNull;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.CacheManager;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;

/**
 * BeanPostProcessor that wraps existing CacheManager beans with EnhancedCacheManager
 * to provide additional features.
 *
 * @since 24.11.2025
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
class CacheManagerBeanPostProcessor implements BeanPostProcessor {

    private final ObjectProvider<AxelixMetricsPublisher> metricsPublisherObjectProvider;

    CacheManagerBeanPostProcessor(ObjectProvider<AxelixMetricsPublisher> metricsPublisherObjectProvider) {
        this.metricsPublisherObjectProvider = metricsPublisherObjectProvider;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (!(bean instanceof CacheManager) || bean instanceof EnhancedCacheManager) {
            return bean;
        }
        return createEnhancedCacheManagerProxy((CacheManager) bean, beanName);
    }

    private Object createEnhancedCacheManagerProxy(CacheManager target, String beanName) {
        DefaultEnhancedCacheManager delegate =
                new DefaultEnhancedCacheManager(beanName, target, metricsPublisherObjectProvider.getIfAvailable());

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(target);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvisor(new DefaultIntroductionAdvisor(
                new EnhancedCacheManagerIntroduction(delegate), EnhancedCacheManager.class));

        return proxyFactory.getProxy();
    }
}
