package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableCaching
public class AxileCachingAutoConfiguration {

    @Bean("axileCacheManager")
    @ConditionalOnMissingBean
    public CacheManager axileCacheManager() {
        return new ConcurrentMapCacheManager("axile-configprops-cache");
    }
}
