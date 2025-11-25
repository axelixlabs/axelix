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
