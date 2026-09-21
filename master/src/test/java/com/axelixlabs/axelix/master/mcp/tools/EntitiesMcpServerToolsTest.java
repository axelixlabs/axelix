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
package com.axelixlabs.axelix.master.mcp.tools;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.Association;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.AssociationProblem;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.FlaggedAssociation;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.JpaEntities;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.MappedEntity;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EntitiesMcpServerTools}.
 *
 * @author Mikhail Polivakha
 */
class EntitiesMcpServerToolsTest {

    private static final String GROUP_ID = "com.example";
    private static final String ARTIFACT_ID = "orders-service";

    private final DatabaseHistoricalApplicationSnapshotService snapshotService =
            mock(DatabaseHistoricalApplicationSnapshotService.class);

    private final EntitiesMcpServerTools subject = new EntitiesMcpServerTools(new JsonMapper(), snapshotService);

    @Nested
    class WhenEntityNameNotSpecified {

        @Test
        void shouldReturnProfileOfEveryMappedEntity() {
            // given.
            MappedEntity order =
                    entity("Order", "orders", 2, List.of(flagged("Order", "items", AssociationProblem.EAGER_FETCHING)));
            MappedEntity customer = entity("Customer", "customers", 0, List.of());
            stubApplication(new JpaEntities(List.of(order, customer)));

            // when.
            String result = subject.getApplicationEntitiesProfile(GROUP_ID, ARTIFACT_ID, null);

            // then.
            assertThatJson(result).isArray().hasSize(2);
            assertThat(result).contains("Order").contains("Customer");
        }
    }

    @Nested
    class WhenEntityNameSpecified {

        @Test
        void shouldReturnOnlyProfileOfMatchingEntity() {
            // given.
            MappedEntity order =
                    entity("Order", "orders", 3, List.of(flagged("Order", "items", AssociationProblem.EAGER_FETCHING)));
            MappedEntity customer = entity("Customer", "customers", 0, List.of());
            stubApplication(new JpaEntities(List.of(order, customer)));

            // when.
            String result = subject.getApplicationEntitiesProfile(GROUP_ID, ARTIFACT_ID, "Order");

            // then.
            assertThatJson(result).node("name").isEqualTo("Order");
            assertThatJson(result).node("table").isEqualTo("orders");
            assertThatJson(result).node("associationsCount").isEqualTo(3);
            assertThatJson(result).node("flaggedAssociations").isArray().hasSize(1);
            assertThatJson(result)
                    .node("flaggedAssociations[0].association.field")
                    .isEqualTo("items");
            assertThatJson(result)
                    .node("flaggedAssociations[0].problems")
                    .isArray()
                    .containsExactly("EAGER_FETCHING");
            assertThat(result).doesNotContain("Customer");
        }

        @Test
        void shouldReturnMessageWhenNoMatchingEntity() {
            // given.
            stubApplication(new JpaEntities(List.of(entity("Customer", "customers", 0, List.of()))));

            // when.
            String result = subject.getApplicationEntitiesProfile(GROUP_ID, ARTIFACT_ID, "Order");

            // then.
            assertThat(result).contains("No entity named 'Order'");
        }
    }

    @Nested
    class ApplicationLookup {

        @Test
        void shouldReturnMessageWhenApplicationNotIdentified() {
            // given. no group / artifact provided.

            // when.
            String result = subject.getApplicationEntitiesProfile(null, null, "Order");

            // then.
            assertThat(result).contains("Provide both 'groupId' and 'artifactId'");
        }

        @Test
        void shouldReturnMessageWhenOnlyGroupIdProvided() {
            // given. artifactId missing.

            // when.
            String result = subject.getApplicationEntitiesProfile(GROUP_ID, null, null);

            // then.
            assertThat(result).contains("Provide both 'groupId' and 'artifactId'");
        }

        @Test
        void shouldReturnMessageWhenApplicationNotFound() {
            // given.
            when(snapshotService.getCurrentRecord(any())).thenReturn(null);

            // when.
            String result = subject.getApplicationEntitiesProfile(GROUP_ID, ARTIFACT_ID, null);

            // then.
            assertThat(result).contains("No application found");
        }

        @Test
        void shouldReturnMessageWhenNoEntitiesMapRecorded() {
            // given.
            stubApplication(null);

            // when.
            String result = subject.getApplicationEntitiesProfile(GROUP_ID, ARTIFACT_ID, null);

            // then.
            assertThat(result).contains("No entities map");
        }
    }

    private void stubApplication(@Nullable JpaEntities jpaEntities) {
        when(snapshotService.getCurrentRecord(ApplicationId.of(GROUP_ID, ARTIFACT_ID)))
                .thenReturn(snapshot(jpaEntities));
    }

    private static MappedEntity entity(
            String name, String table, int associationsCount, List<FlaggedAssociation> flaggedAssociations) {
        return new MappedEntity(name, table, associationsCount, flaggedAssociations);
    }

    private static FlaggedAssociation flagged(String entity, String field, AssociationProblem problem) {
        return new FlaggedAssociation(
                new Association(entity, field), "@ManyToOne(fetch = FetchType.EAGER)", Set.of(problem));
    }

    private static HistoricalApplicationSnapshot snapshot(@Nullable JpaEntities jpaEntities) {
        return new HistoricalApplicationSnapshot(
                new SnapshotId(GROUP_ID, ARTIFACT_ID, LocalDate.now(ZoneOffset.UTC)),
                new Insights(
                        new Insights.HotSpot(
                                new Insights.HotSpot.ProjectLeyden(false, false),
                                new Insights.HotSpot.GarbageCollector(false, GarbageCollector.G1),
                                new Insights.HotSpot.ProjectLilliput(false)),
                        new Insights.SpringFramework(false, null, null),
                        new PersistenceInsights(List.of(), jpaEntities)),
                "1.0.0");
    }
}
