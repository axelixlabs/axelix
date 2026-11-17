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
package com.axelixlabs.axelix.master.service.dashboard;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for startup and scheduled dashboard cache population.
 *
 * @author Vyacheslav Yanin
 */
class DashboardRefreshSchedulerTest {

    private static final List<String> DASHBOARDS = List.of(
            DashboardCaches.PERSISTENCE,
            DashboardCaches.DEPENDENCIES,
            DashboardCaches.SPRING_PORTFOLIO,
            DashboardCaches.LANGUAGES);

    @Nested
    @SpringBootTest(properties = "axelix.master.dashboard.refresh-schedule=-")
    class OnStartup {

        @Autowired
        private CacheManager cacheManager;

        @Test // GH-1844
        void shouldPopulateDashboardCachesOnApplicationStartup() {
            // when / then.
            for (String dashboard : DASHBOARDS) {
                assertThat(Objects.requireNonNull(cacheManager.getCache(dashboard)).get(SimpleKey.EMPTY))
                        .as(dashboard)
                        .isNotNull();
            }
        }
    }

    @Nested
    @SpringBootTest(properties = "axelix.master.dashboard.refresh-schedule=*/1 * * * * *")
    @Import(SchedulingConfiguration.class)
    class OnSchedule {

        @Autowired
        private CacheManager cacheManager;

        @Test // GH-1844
        void shouldRefreshDashboardCachesAtConfiguredInterval() {
            // given.
            Map<String, Object> previous = new HashMap<>();
            for (String dashboard : DASHBOARDS) {
                previous.put(
                        dashboard,
                        Objects.requireNonNull(
                                        Objects.requireNonNull(cacheManager.getCache(dashboard)).get(SimpleKey.EMPTY))
                                .get());
            }

            // when / then.
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                for (String dashboard : DASHBOARDS) {
                    assertThat(Objects.requireNonNull(
                                            Objects.requireNonNull(cacheManager.getCache(dashboard)).get(SimpleKey.EMPTY))
                                    .get())
                            .as(dashboard)
                            .isNotSameAs(previous.get(dashboard));
                }
            });
        }
    }

    @TestConfiguration
    @EnableScheduling
    static class SchedulingConfiguration {}
}
