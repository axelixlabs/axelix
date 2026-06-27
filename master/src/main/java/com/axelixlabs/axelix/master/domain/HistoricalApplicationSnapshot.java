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
package com.axelixlabs.axelix.master.domain;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

/**
 * The insights of the given application, identified by the {@link ApplicationId}.
 * The {@link Insights} are stored with the granularity of 1 day (by {@code date} being the
 * {@link LocalDate}). It is assumed we will not need the granularity of insights to be more
 * than 24 hours.
 *
 * @param insights the actual insights.
 *
 * @author Mikhail Polivakha
 */
@Table("historical_application_snapshots")
public record HistoricalApplicationSnapshot(
        @Id @Embedded.Empty SnapshotId snapshotId,
        @Embedded.Empty Insights insights) {

    /**
     * The composite key ID.
     *
     * @param groupId the group id of the application.
     * @param artifactId the artifact id of the application.
     * @param date the date, for which we consider the provided {@link #insights()} being actual.
     */
    // TODO:
    //  We should use the ApplicationId here, but we cannot now do that since we need Spring Data JDBC 4.1
    //  that supports embedded fields in the composite keys
    public record SnapshotId(String groupId, String artifactId, LocalDate date) {}
}
