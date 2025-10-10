package com.nucleonforge.axile.spring.beans;

import com.nucleonforge.axile.common.api.BeansFeed;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Decorator for BeanAnalyzer that caches analysis results.
 *
 * @since 10.10.2025
 * @author Nikita Kirillov
 */
public class CachingBeanEnricher implements BeanEnricher {

    private final BeanEnricher delegate;
    private final ConcurrentMap<CacheKey, Optional<BeansFeed.Bean>> cache;

    public CachingBeanEnricher(BeanEnricher delegate) {
        this.delegate = delegate;
        this.cache = new ConcurrentHashMap<>(500);
    }

    @Override
    public Optional<BeansFeed.Bean> enrich(String beanName, BeansEndpoint.BeanDescriptor beanDescriptor,
                                           ApplicationContext context) {
        String contextId = context.getId();
        CacheKey key = new CacheKey(beanName, contextId);

        return cache.computeIfAbsent(key, k ->
            delegate.enrich(beanName, beanDescriptor, context)
        );
    }

    public void clearCache() {
        cache.clear();
    }

    /**
     * Cache key combining bean name and context ID.
     */
    private record CacheKey(String beanName, String contextId) {}
}
