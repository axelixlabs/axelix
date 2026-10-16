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
package com.axelixlabs.axelix.master.utils;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;

/**
 * Inserts a {@link HistoricalApplicationSnapshot} row for tests.
 */
public final class TestHistoricalApplicationSnapshotFactory {

    private TestHistoricalApplicationSnapshotFactory() {}

    public static void insert(
            JdbcAggregateTemplate jdbcAggregateTemplate, String groupId, String artifactId, LocalDate date) {
        jdbcAggregateTemplate.insert(new HistoricalApplicationSnapshot(
                new SnapshotId(groupId, artifactId, date),
                new Insights(
                        new Insights.HotSpot(
                                new Insights.HotSpot.ProjectLeyden(false, false),
                                new Insights.HotSpot.GarbageCollector(false, GarbageCollector.UNKNOWN),
                                new Insights.HotSpot.ProjectLilliput(false)),
                        new Insights.SpringFramework(false),
                        new PersistenceInsights(List.of()))));
    }
}
