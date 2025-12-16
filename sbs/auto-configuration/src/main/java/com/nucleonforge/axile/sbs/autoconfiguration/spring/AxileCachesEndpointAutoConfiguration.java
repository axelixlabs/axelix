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
package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import java.util.Map;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.cache.CachesEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.cache.AxileCachesEndpoint;
import com.nucleonforge.axile.sbs.spring.cache.CacheDispatcher;
import com.nucleonforge.axile.sbs.spring.cache.CacheManagerBeanPostProcessor;
import com.nucleonforge.axile.sbs.spring.cache.DefaultCacheDispatcher;

/**
 * {@code CacheDispatcherAutoConfiguration} provides auto-configuration
 * for cache dispatching and exposing cache-related operations via a custom Actuator endpoint.
 *
 * <p>This configuration registers the following beans if they are not already defined in the context:
 * <ul>
 *     <li>{@link DefaultCacheDispatcher} — dispatcher that coordinates cache operations across
 *  *     all registered {@link CacheManager} beans,</li>
 *     <li>{@link AxileCachesEndpoint} — a custom Spring Boot Actuator endpoint for cache management.</li>
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
 */
@AutoConfiguration(after = {CacheAutoConfiguration.class, CachesEndpoint.class})
@ConditionalOnAvailableEndpoint(endpoint = CachesEndpoint.class)
@ConditionalOnBean(CacheManager.class)
public class AxileCachesEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheDispatcher cacheDispatcher(Map<String, CacheManager> managerMap) {
        return new DefaultCacheDispatcher(managerMap);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxileCachesEndpoint cacheDispatcherEndpoint(CacheDispatcher dispatcher, CachesEndpoint delegate) {
        return new AxileCachesEndpoint(dispatcher, delegate);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheManagerBeanPostProcessor cacheManagerBeanPostProcessor() {
        return new CacheManagerBeanPostProcessor();
    }
}
