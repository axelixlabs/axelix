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

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse.TreemapEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link InMemoryMapDashboardCache}.
 *
 * @author Vyacheslav Yanin
 */
class DashboardCacheTest {

    private static final Instant COMPUTED_AT = Instant.parse("2026-01-01T00:00:00Z");

    private final DashboardCache subject = new InMemoryMapDashboardCache();

    @Test
    void shouldComputeAndCacheValueOnFirstGet() {
        // given.
        PersistenceDashboardResponse response = persistenceResponse("service-a");
        var loads = new AtomicInteger();

        // when.
        PersistenceDashboardResponse first = subject.get(
                DashboardKey.PERSISTENCE,
                PersistenceDashboardResponse.class,
                () -> {
                    loads.incrementAndGet();
                    return response;
                });
        PersistenceDashboardResponse second = subject.get(
                DashboardKey.PERSISTENCE,
                PersistenceDashboardResponse.class,
                () -> {
                    loads.incrementAndGet();
                    return response;
                });

        // then.
        assertThat(first).isSameAs(response);
        assertThat(second).isSameAs(response);
        assertThat(loads).hasValue(1);
    }

    @Test
    void shouldReturnExplicitlyPutValueWithoutCallingLoader() {
        // given.
        PersistenceDashboardResponse response = persistenceResponse("service-a");
        subject.put(DashboardKey.PERSISTENCE, response);

        // when.
        PersistenceDashboardResponse result = subject.get(
                DashboardKey.PERSISTENCE,
                PersistenceDashboardResponse.class,
                () -> {
                    throw new AssertionError("The loader must not be called for a cached value");
                });

        // then.
        assertThat(result).isSameAs(response);
    }

    @Test
    void shouldEvictValue() {
        // given.
        PersistenceDashboardResponse cached = persistenceResponse("service-a");
        PersistenceDashboardResponse fresh = persistenceResponse("service-b");
        subject.put(DashboardKey.PERSISTENCE, cached);

        // when.
        subject.evict(DashboardKey.PERSISTENCE);
        PersistenceDashboardResponse result =
                subject.get(DashboardKey.PERSISTENCE, PersistenceDashboardResponse.class, () -> fresh);

        // then.
        assertThat(result).isSameAs(fresh);
    }

    @Test
    void shouldClearAllValues() {
        // given.
        subject.put(DashboardKey.PERSISTENCE, persistenceResponse("service-a"));
        subject.put(DashboardKey.LANGUAGES, persistenceResponse("service-b"));
        AtomicInteger loads = new AtomicInteger();

        // when.
        subject.clear();
        subject.get(DashboardKey.PERSISTENCE, PersistenceDashboardResponse.class, () -> {
            loads.incrementAndGet();
            return persistenceResponse("service-c");
        });
        subject.get(DashboardKey.LANGUAGES, PersistenceDashboardResponse.class, () -> {
            loads.incrementAndGet();
            return persistenceResponse("service-d");
        });

        // then.
        assertThat(loads).hasValue(2);
    }

    @Test
    void shouldRejectNullLoaderResult() {
        assertThatThrownBy(() -> subject.get(DashboardKey.PERSISTENCE, Object.class, () -> null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectValueOfUnexpectedType() {
        // given.
        subject.put(DashboardKey.PERSISTENCE, "not a dashboard response");

        // when/then.
        assertThatThrownBy(() -> subject.get(
                        DashboardKey.PERSISTENCE,
                        PersistenceDashboardResponse.class,
                        () -> persistenceResponse("service-a")))
                .isInstanceOf(ClassCastException.class);
    }

    private static PersistenceDashboardResponse persistenceResponse(String appName) {
        return new PersistenceDashboardResponse(
                List.of(new TreemapEntry(appName, 1)), List.of(), COMPUTED_AT);
    }
}
