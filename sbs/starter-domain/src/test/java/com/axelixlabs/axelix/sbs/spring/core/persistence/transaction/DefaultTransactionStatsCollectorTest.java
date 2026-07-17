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
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.persistence.MethodClassKey;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleSqlQueryRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LazyLoadingTarget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultTransactionStatsCollector}.
 *
 * @author Mikhail Polivakha
 */
class DefaultTransactionStatsCollectorTest {

    private DefaultTransactionStatsCollector subject;

    private MethodClassKey doWorkKey;
    private MethodClassKey otherWorkKey;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        subject = new DefaultTransactionStatsCollector();
        doWorkKey = new MethodClassKey(SampleService.class.getDeclaredMethod("doWork"), SampleService.class);
        otherWorkKey = new MethodClassKey(SampleService.class.getDeclaredMethod("otherWork"), SampleService.class);
    }

    @Nested
    class RecordTransaction {

        @Test
        void shouldCreateStatsEntryForNewMethodKey() {
            // given.
            TransactionExecutionProfile profile = completedProfile(simpleQuery("select 1"));

            // when.
            subject.recordTransaction(doWorkKey, profile);

            // then.
            Map<MethodClassKey, TransactionStats> stats = subject.getCopyOfStats();
            assertThat(stats).containsOnlyKeys(doWorkKey);
            assertThat(stats.get(doWorkKey).getNPlusOneOccasions()).isEmpty();
            assertThat(stats.get(doWorkKey).getInMemoryPaginatedEntities()).isEmpty();
        }

        @Test
        void shouldKeepSeparateStatsForDifferentMethodKeys() {
            // given.
            TransactionExecutionProfile first = completedProfile(simpleQuery("select 1"));
            TransactionExecutionProfile second = completedProfile(simpleQuery("select 2"));

            // when.
            subject.recordTransaction(doWorkKey, first);
            subject.recordTransaction(otherWorkKey, second);

            // then.
            assertThat(subject.getCopyOfStats()).containsOnlyKeys(doWorkKey, otherWorkKey);
        }

        @Test
        void shouldAggregateIntoExistingStatsWhenSameMethodKeyIsRecordedAgain() {
            // given.
            LazyLoadingTarget pets = new LazyLoadingTarget(SampleEntity.class, "pets");
            subject.recordTransaction(doWorkKey, profileWithNPlusOne(pets, 1));
            subject.recordTransaction(doWorkKey, profileWithNPlusOne(pets, 3));

            // when.
            TransactionStats stats = subject.getCopyOfStats().get(doWorkKey);

            // then.
            assertThat(subject.getCopyOfStats()).containsOnlyKeys(doWorkKey);
            assertThat(stats.getNPlusOneOccasions()).containsEntry(pets, 3);
        }

        @Test
        void shouldAggregateInMemoryPaginationIntoExistingStats() {
            // given.
            subject.recordTransaction(doWorkKey, completedProfile(inMemoryPaginatedQuery("select * from owner o")));
            subject.recordTransaction(
                    doWorkKey,
                    completedProfile(
                            inMemoryPaginatedQuery("select * from owner o1"),
                            inMemoryPaginatedQuery("select * from owner o2")));

            // when.
            TransactionStats stats = subject.getCopyOfStats().get(doWorkKey);

            // then.
            assertThat(stats.getInMemoryPaginatedEntities()).containsEntry("owner", 2);
        }
    }

    @Nested
    class GetCopyOfStats {

        @Test
        void shouldReturnEmptyMapWhenNothingHasBeenRecorded() {
            // given.
            // fresh subject from @BeforeEach

            // when.
            Map<MethodClassKey, TransactionStats> stats = subject.getCopyOfStats();

            // then.
            assertThat(stats).isEmpty();
        }

        @Test
        void shouldReturnUnmodifiableView() {
            // given.
            subject.recordTransaction(doWorkKey, completedProfile(simpleQuery("select 1")));

            // when.
            Map<MethodClassKey, TransactionStats> stats = subject.getCopyOfStats();

            // then.
            assertThatThrownBy(stats::clear).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> stats.put(otherWorkKey, new TransactionStats()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class Clear {

        @Test
        void shouldRemoveAllCollectedStats() {
            // given.
            subject.recordTransaction(doWorkKey, completedProfile(simpleQuery("select 1")));
            subject.recordTransaction(otherWorkKey, completedProfile(simpleQuery("select 2")));

            // when.
            subject.clear();

            // then.
            assertThat(subject.getCopyOfStats()).isEmpty();
        }

        @Test
        void shouldAllowRecordingAfterClear() {
            // given.
            subject.recordTransaction(doWorkKey, completedProfile(simpleQuery("select 1")));
            subject.clear();

            // when.
            subject.recordTransaction(otherWorkKey, completedProfile(simpleQuery("select 2")));

            // then.
            assertThat(subject.getCopyOfStats()).containsOnlyKeys(otherWorkKey);
        }

        @Test
        void shouldBeNoOpWhenAlreadyEmpty() {
            // given.
            // fresh subject from @BeforeEach

            // when.
            subject.clear();

            // then.
            assertThat(subject.getCopyOfStats()).isEmpty();
        }
    }

    private static TransactionExecutionProfile completedProfile(SimpleSqlQueryRecord... queries) {
        TransactionExecutionProfile profile = new TransactionExecutionProfile(Instant.now());
        for (SimpleSqlQueryRecord query : queries) {
            profile.recordQuery(query);
        }
        return profile.complete();
    }

    private static TransactionExecutionProfile profileWithNPlusOne(LazyLoadingTarget target, int occasions) {
        TransactionExecutionProfile profile = new TransactionExecutionProfile(Instant.now());
        for (int i = 0; i < occasions; i++) {
            profile.recordQuery(simpleQuery("select * from pet where owner_id = " + i));
            profile.recordLazyLoading(
                    new LazyLoadingTarget(target.ownerEntityClass(), target.associationPropertyName()));
        }
        return profile.complete();
    }

    private static SimpleSqlQueryRecord simpleQuery(String sql) {
        return new SimpleSqlQueryRecord(sql, 5L, 1_000L, false);
    }

    private static SimpleSqlQueryRecord inMemoryPaginatedQuery(String sql) {
        return new SimpleSqlQueryRecord(sql, 5L, 1_000L, true);
    }

    private static class SampleService {
        void doWork() {}

        void otherWork() {}
    }

    private static class SampleEntity {}
}
