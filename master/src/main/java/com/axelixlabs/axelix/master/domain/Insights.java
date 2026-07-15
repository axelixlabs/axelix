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

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;

/**
 * Insight information discovered for the given service instance.
 *
 * @param hotSpot              the HotSpot-specific insights
 * @param springFramework      the Spring Framework-specific insights
 * @param persistenceInsights  persistence transactional insights received during heartbeat
 *
 * @author Mikhail Polivakha
 */
public record Insights(
        @Embedded.Empty HotSpot hotSpot,
        @Embedded.Empty SpringFramework springFramework,
        @Column("persistence_insights") PersistenceInsights persistenceInsights) {

    /**
     * @param projectLeyden Project Leyden-specific insights
     * @param gc Garbage collector Leyden-specific insights
     * @param projectLilliput Project Lilliput-specific insights
     */
    public record HotSpot(
            @Embedded.Empty ProjectLeyden projectLeyden,
            @Embedded.Empty GarbageCollector gc,
            @Embedded.Empty ProjectLilliput projectLilliput) {

        /**
         * @param appCdsEnabled is App CDS being used.
         * @param aotCacheEnabled is Aot Cache being used.
         */
        public record ProjectLeyden(
                @Column("app_cds_enabled") boolean appCdsEnabled,
                @Column("aot_cache_enabled") boolean aotCacheEnabled) {}

        /**
         * @param gcLoggingEnabled is gc logging enabled
         * @param gcInUse what garbage collector is in use
         */
        public record GarbageCollector(
                @Column("gc_logging_enabled") boolean gcLoggingEnabled,
                @Column("gc_in_use") com.axelixlabs.axelix.common.domain.insights.GarbageCollector gcInUse) {}

        /**
         * @param compactObjectHeadersEnabled are Compact Object Headers enabled.
         */
        public record ProjectLilliput(
                @Column("compact_object_headers_enabled") boolean compactObjectHeadersEnabled) {}
    }

    /**
     * @param osivEnabled is OSIV enabled?
     */
    public record SpringFramework(boolean osivEnabled) {}
}
