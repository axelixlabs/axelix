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
package com.axelixlabs.axelix.sbs.spring.core.persistence.transaction;

import java.time.Instant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.domain.insights.TypeExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleExternalCallRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleSqlQueryRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LazyLoadingTarget;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TransactionStats}.
 *
 * @author Mikhail Polivakha
 */
class TransactionStatsTest {

    @Nested
    class InMemoryPaginationAggregation {

        @Test
        void shouldExtractTableNameAfterFromClause() {
            // given.
            TransactionStats subject = new TransactionStats();
            TransactionExecutionProfile profile =
                    profileWith(inMemoryPaginated("select owner0_.id from owner owner0_ order by owner0_.id"));

            // when.
            subject.put(profile);

            // then.
            assertThat(subject.getInMemoryPaginatedEntities()).containsEntry("owner", 1);
        }

        @Test
        void shouldExtractTableNameWhenFromIsUpperCase() {
            // given.
            TransactionStats subject = new TransactionStats();
            TransactionExecutionProfile profile = profileWith(inMemoryPaginated("SELECT id FROM pet p WHERE p.id = 1"));

            // when.
            subject.put(profile);

            // then.
            assertThat(subject.getInMemoryPaginatedEntities()).containsEntry("pet", 1);
        }

        @Test
        void shouldSkipInMemoryPaginatedQueryWithoutFromClause() {
            // given.
            TransactionStats subject = new TransactionStats();
            TransactionExecutionProfile profile = profileWith(inMemoryPaginated("select 1"));

            // when.
            subject.put(profile);

            // then.
            assertThat(subject.getInMemoryPaginatedEntities()).isEmpty();
        }

        @Test
        void shouldKeepMaxCountAcrossTransactionsForSameTarget() {
            // given.
            TransactionStats subject = new TransactionStats();

            // when.
            subject.put(profileWith(
                    inMemoryPaginated("select * from owner o"), inMemoryPaginated("select * from owner o2")));
            subject.put(profileWith(inMemoryPaginated("select * from owner o")));

            // then.
            assertThat(subject.getInMemoryPaginatedEntities()).containsEntry("owner", 2);
        }
    }

    @Nested
    class NPlusOneAggregation {

        @Test
        void shouldAggregateIdenticalLazyLoadingTargets() {
            // given.
            TransactionStats subject = new TransactionStats();
            // new LazyLoadingTarget objects here are used on purpose so that we want to check for
            // the LazyLoadingTarget equal/hashcode contract.
            LazyLoadingTarget pets = new LazyLoadingTarget(SampleEntity.class, "pets");

            TransactionExecutionProfile profile = new TransactionExecutionProfile(Instant.now());
            profile.recordQuery(simple("select * from owner"));
            profile.recordLazyLoading(new LazyLoadingTarget(SampleEntity.class, "pets"));
            profile.recordQuery(simple("select * from pet where owner_id = 1"));
            profile.recordLazyLoading(new LazyLoadingTarget(SampleEntity.class, "pets"));
            profile.complete();

            // when.
            subject.put(profile);

            // then.
            assertThat(subject.getNPlusOneOccasions()).containsEntry(pets, 2);
            assertThat(subject.getInMemoryPaginatedEntities()).isEmpty();
        }
    }

    @Nested
    class ExternalCallAggregation {

        @Test
        void shouldAggregateCallsToTheSameEndpointWithinATransaction() {
            // given.
            TransactionStats subject = new TransactionStats();
            TransactionExecutionProfile profile = profileWithExternalCalls(externalCall(10L), externalCall(30L));

            // when.
            subject.put(profile);

            // then.
            assertThat(subject.getExternalCalls()).singleElement().satisfies(aggregatedCall -> {
                assertThat(aggregatedCall.getType()).isEqualTo(TypeExternalCall.HTTP_CLIENT);
                assertThat(aggregatedCall.getTarget()).isEqualTo("GET https://payments/charge");
                assertThat(aggregatedCall.getStats().getMinMs()).isEqualTo(10L);
                assertThat(aggregatedCall.getStats().getMaxMs()).isEqualTo(30L);
                assertThat(aggregatedCall.getStats().getAvgMs()).isEqualTo(20L);
            });
        }

        @Test
        void shouldAggregateTheSameEndpointAcrossTransactions() {
            // given.
            TransactionStats subject = new TransactionStats();

            // when.
            subject.put(profileWithExternalCalls(externalCall(10L), externalCall(30L)));
            subject.put(profileWithExternalCalls(externalCall(50L)));

            // then. Recording every raw call across both invocations: min/max span both, avg is the running mean.
            assertThat(subject.getExternalCalls()).singleElement().satisfies(aggregatedCall -> {
                assertThat(aggregatedCall.getStats().getMinMs()).isEqualTo(10L);
                assertThat(aggregatedCall.getStats().getMaxMs()).isEqualTo(50L);
                assertThat(aggregatedCall.getStats().getAvgMs()).isEqualTo(30L);
            });
        }
    }

    private static TransactionExecutionProfile profileWithExternalCalls(SimpleExternalCallRecord... calls) {
        TransactionExecutionProfile profile = new TransactionExecutionProfile(Instant.now());
        for (SimpleExternalCallRecord call : calls) {
            profile.recordExternalCall(call);
        }
        return profile.complete();
    }

    private static SimpleExternalCallRecord externalCall(long durationMs) {
        return new SimpleExternalCallRecord(TypeExternalCall.HTTP_CLIENT, "GET https://payments/charge", durationMs);
    }

    private static TransactionExecutionProfile profileWith(SimpleSqlQueryRecord... queries) {
        TransactionExecutionProfile profile = new TransactionExecutionProfile(Instant.now());
        for (SimpleSqlQueryRecord query : queries) {
            profile.recordQuery(query);
        }
        return profile.complete();
    }

    private static SimpleSqlQueryRecord inMemoryPaginated(String sql) {
        return new SimpleSqlQueryRecord(sql, 5L, 1_000L, true);
    }

    private static SimpleSqlQueryRecord simple(String sql) {
        return new SimpleSqlQueryRecord(sql, 5L, 1_000L, false);
    }

    private static class SampleEntity {}
}
