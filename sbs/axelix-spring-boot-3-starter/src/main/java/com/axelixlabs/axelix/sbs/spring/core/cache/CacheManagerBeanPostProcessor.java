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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.CacheManager;
import org.springframework.util.ClassUtils;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.utils.ProxyingUtils;

/**
 * BeanPostProcessor that wraps existing CacheManager beans with EnhancedCacheManager
 * to provide additional features.
 *
 * @since 24.11.2025
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
public class CacheManagerBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(CacheManagerBeanPostProcessor.class);

    private final ObjectProvider<AxelixMetricsPublisher> metricsPublisherObjectProvider;

    public CacheManagerBeanPostProcessor(ObjectProvider<AxelixMetricsPublisher> metricsPublisherObjectProvider) {
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

        Advisor advisor = new DefaultIntroductionAdvisor(
                new EnhancedCacheManagerIntroduction(delegate), EnhancedCacheManager.class);

        return ProxyingUtils.tryCreateProxy(target, advisor).orElseGet(() -> {
            log.warn(
                    "Cannot enable enhanced cache management for CacheManager bean '{}' of class {}: CGLIB cannot "
                            + "proxy a final class or override a final method. Enhanced cache features are skipped "
                            + "for this bean; the bean itself and every other Axelix feature are unaffected.",
                    beanName,
                    ClassUtils.getUserClass(target.getClass()).getName());
            return target;
        });
    }
}
