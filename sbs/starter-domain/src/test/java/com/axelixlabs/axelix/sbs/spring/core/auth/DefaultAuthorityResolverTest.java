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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultAuthorityResolver}.
 *
 * @author Cherkasov Sergey
 */
public class DefaultAuthorityResolverTest {

    private AuthorityResolver authorityResolver;

    @BeforeEach
    void setUp() {
        authorityResolver = new DefaultAuthorityResolver((pathTemplate, actualPath) -> {
            PathPattern pattern = new PathPatternParser().parse(pathTemplate);
            return pattern.matchAndExtract(PathContainer.parsePath(actualPath)) != null;
        });
    }

    @Test
    void shouldResolve_EnvValuesRead() {
        assertThat(authorityResolver.resolve("/axelix-env")).contains(DefaultAuthority.ENV_VALUES_READ);
    }

    @Test
    void shouldResolve_ConfiPropsValuesRead() {
        assertThat(authorityResolver.resolve("/axelix-configprops"))
                .contains(DefaultAuthority.CONFIG_PROPS_VALUES_READ);
    }

    @Test
    void shouldResolve_PropertyValueMutate() {
        assertThat(authorityResolver.resolve("/axelix-property-management"))
                .contains(DefaultAuthority.PROPERTY_VALUE_MUTATE);
    }

    @Test
    void shouldResolve_ScheduledTaskModify() {
        // Cron Expression
        assertThat(authorityResolver.resolve("/axelix-scheduled-tasks/modify/cron-expression"))
                .contains(DefaultAuthority.SCHEDULED_TASKS_MODIFY);

        // Interval
        assertThat(authorityResolver.resolve("/axelix-scheduled-tasks/modify/interval"))
                .contains(DefaultAuthority.SCHEDULED_TASKS_MODIFY);

        // Enable
        assertThat(authorityResolver.resolve("/axelix-scheduled-tasks/enable"))
                .contains(DefaultAuthority.SCHEDULED_TASKS_MODIFY);

        // Disable
        assertThat(authorityResolver.resolve("/axelix-scheduled-tasks/disable"))
                .contains(DefaultAuthority.SCHEDULED_TASKS_MODIFY);

        // Execute
        assertThat(authorityResolver.resolve("/axelix-scheduled-tasks/execute"))
                .contains(DefaultAuthority.SCHEDULED_TASKS_MODIFY);
    }

    @Test
    void shouldResolve_ConditionRead() {
        assertThat(authorityResolver.resolve("/axelix-conditions")).contains(DefaultAuthority.CONDITIONS_READ);
    }

    @Test
    void shouldResolve_CachesClear() {
        // All cache clear
        assertThat(authorityResolver.resolve("/axelix-caches/clear")).contains(DefaultAuthority.CACHES_CLEAR);

        // Single cache clear
        assertThat(authorityResolver.resolve("/axelix-caches/{cacheManagerName}/{cacheName}/clear"))
                .contains(DefaultAuthority.CACHES_CLEAR);
        assertThat(authorityResolver.resolve("/axelix-caches/cacheManager/cacheName/clear"))
                .contains(DefaultAuthority.CACHES_CLEAR);

        // Single cache manager clear
        assertThat(authorityResolver.resolve("/axelix-caches/cacheManager/clear-all"))
                .contains(DefaultAuthority.CACHES_CLEAR);
    }

    @Test
    void shouldResolve_CachesToggle() {
        // Single cache
        assertThat(authorityResolver.resolve("/axelix-caches/{cacheManagerName}/{cacheName}/enable"))
                .contains(DefaultAuthority.CACHES_TOGGLE);
        assertThat(authorityResolver.resolve("/axelix-caches/cacheManager/cacheName/enable"))
                .contains(DefaultAuthority.CACHES_TOGGLE);
        assertThat(authorityResolver.resolve("/axelix-caches/{cacheManagerName}/{cacheName}/disable"))
                .contains(DefaultAuthority.CACHES_TOGGLE);
        assertThat(authorityResolver.resolve("/axelix-caches/cacheManager/cacheName/disable"))
                .contains(DefaultAuthority.CACHES_TOGGLE);

        // Single cache manager
        assertThat(authorityResolver.resolve("/axelix-caches/cacheManager/enable"))
                .contains(DefaultAuthority.CACHES_TOGGLE);
        assertThat(authorityResolver.resolve("/axelix-caches/cacheManager/disable"))
                .contains(DefaultAuthority.CACHES_TOGGLE);
    }

    @Test
    void shouldResolve_GarbageCollector() {
        // Trigger
        assertThat(authorityResolver.resolve("/axelix-gc/trigger")).contains(DefaultAuthority.GARBAGE_COLLECTOR);

        // GC Logs monitoring
        assertThat(authorityResolver.resolve("/axelix-gc/log/enable")).contains(DefaultAuthority.GARBAGE_COLLECTOR);
        assertThat(authorityResolver.resolve("/axelix-gc/log/disable")).contains(DefaultAuthority.GARBAGE_COLLECTOR);
    }

    @Test
    void shouldReturnEmpty_ScheduledTasksRead() {
        assertThat(authorityResolver.resolve("/axelix-scheduled-tasks")).isEmpty();
    }

    @Test
    void shouldReturnEmpty_Caches() {
        assertThat(authorityResolver.resolve("/axelix-caches")).isEmpty();
    }

    @Test
    void shouldReturnEmpty_GarbageCollector() {
        assertThat(authorityResolver.resolve("/axelix-gc")).isEmpty();
    }

    @Test
    void shouldReturnEmpty_GarbageCollectorRead() {
        assertThat(authorityResolver.resolve("/axelix-gc/log/status")).isEmpty();
        assertThat(authorityResolver.resolve("/axelix-gc/log/file")).isEmpty();
    }

    @Test
    void shouldReturnEmpty_BeansRead() {
        assertThat(authorityResolver.resolve("/axelix-beans")).isEmpty();
    }

    @Test
    void shouldReturnEmpty_HeapDumpRead() {
        assertThat(authorityResolver.resolve("/axelix-heap-dump")).isEmpty();
    }

    @Test
    void shouldReturnEmpty_DetailsRead() {
        assertThat(authorityResolver.resolve("/axelix-details")).isEmpty();
    }

    @Test
    void shouldReturnEmpty_MetadataRead() {
        assertThat(authorityResolver.resolve("/axelix-metadata")).isEmpty();
    }

    @Test
    void shouldReturnEmpty_Loggers() {
        assertThat(authorityResolver.resolve("/axelix-loggers")).isEmpty();
        assertThat(authorityResolver.resolve("/axelix-loggers/{logger.name}")).isEmpty();
        assertThat(authorityResolver.resolve("/axelix-loggers/logger.name")).isEmpty();
        assertThat(authorityResolver.resolve("/axelix-loggers/reset/{logger.name}"))
                .isEmpty();
        assertThat(authorityResolver.resolve("/axelix-loggers/reset/logger.name"))
                .isEmpty();
    }

    @Test
    void shouldReturnEmpty_Metrics() {
        assertThat(authorityResolver.resolve("/axelix-metrics")).isEmpty();
        assertThat(authorityResolver.resolve("/axelix-metrics/{metric.name}")).isEmpty();
        assertThat(authorityResolver.resolve("/axelix-metrics/jvm.buffer.count"))
                .isEmpty();
    }

    @Test
    void shouldReturnEmpty_ThreadDump() {
        assertThat(authorityResolver.resolve("/axelix-thread-dump")).isEmpty();
        assertThat(authorityResolver.resolve("/axelix-thread-dump/enable")).isEmpty();
        assertThat(authorityResolver.resolve("/axelix-thread-dump/disable")).isEmpty();
    }

    @Test
    void shouldReturnEmpty_TransactionMonitoring() {
        assertThat(authorityResolver.resolve("/axelix-transactions-monitoring")).isEmpty();
    }

    @Test
    void shouldReturnEmpty_FeignClient() {
        assertThat(authorityResolver.resolve("/axelix-feign")).isEmpty();
    }
}
