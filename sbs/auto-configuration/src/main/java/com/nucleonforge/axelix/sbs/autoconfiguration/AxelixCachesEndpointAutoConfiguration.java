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
package com.nucleonforge.axelix.sbs.autoconfiguration;

import java.util.Map;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.sbs.spring.cache.AxelixCachesEndpoint;
import com.nucleonforge.axelix.sbs.spring.cache.CacheManagerBeanPostProcessor;
import com.nucleonforge.axelix.sbs.spring.cache.CacheOperationsDispatcher;
import com.nucleonforge.axelix.sbs.spring.cache.CacheSizeProvider;
import com.nucleonforge.axelix.sbs.spring.cache.DefaultCacheOperationsDispatcher;
import com.nucleonforge.axelix.sbs.spring.cache.DefaultCacheSizeProvider;

/**
 * {@code CacheDispatcherAutoConfiguration} provides auto-configuration
 * for cache dispatching and exposing cache-related operations via a custom Actuator endpoint.
 *
 * <p>This configuration registers the following beans if they are not already defined in the context:
 * <ul>
 *     <li>{@link DefaultCacheOperationsDispatcher} — dispatcher that coordinates cache operations across
 *  *     all registered {@link CacheManager} beans,</li>
 *     <li>{@link AxelixCachesEndpoint} — a custom Spring Boot Actuator endpoint for cache management.</li>
 * </ul>
 * <p>Auto-configuration is only activated if a {@link CacheManager}
 * bean is available in the application context.
 *
 * <p>This class is intended to be registered via Spring Boot's auto-configuration mechanism,
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * (for Spring Boot 3.x+).
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@AutoConfiguration(after = {CacheAutoConfiguration.class})
public class AxelixCachesEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheSizeProvider cacheSizeProvider() {
        return new DefaultCacheSizeProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheOperationsDispatcher cacheDispatcher(
            // we have to inject here by CacheManager rather than by EnhancedCacheManager, since the bean definition
            // in spring for the EnhancedCacheManager still has CacheManager type.
            Map<String, CacheManager> managerMap, CacheSizeProvider cacheSizeProvider) {
        return new DefaultCacheOperationsDispatcher(managerMap, cacheSizeProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixCachesEndpoint cacheDispatcherEndpoint(CacheOperationsDispatcher dispatcher) {
        return new AxelixCachesEndpoint(dispatcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheManagerBeanPostProcessor cacheManagerBeanPostProcessor() {
        return new CacheManagerBeanPostProcessor();
    }
}
