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
package com.axelixlabs.axelix.sbs.spring.core.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionExecutionProfile.AnalyzedSqlQueryRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link TransactionAccessor}.
 *
 * @author Mikhail Polivakha
 */
class TransactionAccessorTest {

    private final TransactionAccessor subject = new TransactionAccessor();

    @AfterEach
    void tearDown() {
        // The accessor keeps its state in a static ThreadLocal, so we must reset it between tests
        // to keep them isolated from each other.
        subject.clearAll();
    }

    private static SimpleSqlQueryRecord query(String sql) {
        return new SimpleSqlQueryRecord(sql, 5L, 1_000L, false);
    }

    @Nested
    class RecordSqlQuery {

        @Test
        void shouldRecordQueryIntoActiveTransaction() {
            // given.
            subject.recordNewTransactionStarted();

            // when.
            subject.recordSqlQuery(query("select 1"));
            TransactionExecutionProfile profile = subject.recordTransactionCompletion();

            // then.
            assertThat(profile.getRecordedQueries())
                    .hasSize(1)
                    .extracting(AnalyzedSqlQueryRecord::getSql)
                    .containsExactly("select 1");
        }

        @Test
        void shouldRecordMultipleQueriesPreservingOrder() {
            // given.
            subject.recordNewTransactionStarted();

            // when.
            subject.recordSqlQuery(query("select 1"));
            subject.recordSqlQuery(query("select 2"));
            subject.recordSqlQuery(query("select 3"));
            TransactionExecutionProfile profile = subject.recordTransactionCompletion();

            // then.
            assertThat(profile.getRecordedQueries())
                    .hasSize(3)
                    .extracting(AnalyzedSqlQueryRecord::getSql)
                    .containsExactly("select 1", "select 2", "select 3");
        }

        @Test
        void shouldSilentlyIgnoreQueryWhenNoTransactionIsActive() {
            // given.
            // no transaction has been started

            // when then.
            assertThatCode(() -> subject.recordSqlQuery(query("select 1"))).doesNotThrowAnyException();
        }

        @Test
        void shouldNotLeakQueryRecordedOutsideTransactionIntoTheNextTransaction() {
            // given.
            subject.recordSqlQuery(query("orphan query"));

            // when.
            subject.recordNewTransactionStarted();
            subject.recordSqlQuery(query("select 1"));
            TransactionExecutionProfile profile = subject.recordTransactionCompletion();

            // then.
            assertThat(profile.getRecordedQueries())
                    .hasSize(1)
                    .extracting(AnalyzedSqlQueryRecord::getSql)
                    .containsExactly("select 1");
        }
    }

    @Nested
    class RecordLazyLoading {

        @Test
        void shouldMarkLastRecordedQueryAsLazilyLoaded() {
            // given.
            LazyLoadingTarget lazyLoadingTarget = new LazyLoadingTarget(SampleEntity.class, "pets");
            subject.recordNewTransactionStarted();
            subject.recordSqlQuery(query("select * from owner"));

            // when.
            subject.recordLazyLoading(lazyLoadingTarget);
            TransactionExecutionProfile profile = subject.recordTransactionCompletion();

            // then.
            assertThat(profile.getRecordedQueries()).hasSize(1);

            AnalyzedSqlQueryRecord recorded = profile.getRecordedQueries().get(0);
            assertThat(recorded.getLazyLoadingTarget()).isNotNull();
            assertThat(recorded.getLazyLoadingTarget().ownerEntityClass()).isEqualTo(SampleEntity.class);
            assertThat(recorded.getLazyLoadingTarget().associationPropertyName())
                    .isEqualTo("pets");
        }

        @Test
        void shouldMarkOnlyTheMostRecentQuery() {
            // given.
            subject.recordNewTransactionStarted();
            subject.recordSqlQuery(query("select 1"));
            subject.recordSqlQuery(query("select 2"));

            // when.
            subject.recordLazyLoading(new LazyLoadingTarget(SampleEntity.class, "tags"));
            TransactionExecutionProfile profile = subject.recordTransactionCompletion();

            // then.
            assertThat(profile.getRecordedQueries()).hasSize(2);
            assertThat(profile.getRecordedQueries().get(0).getLazyLoadingTarget())
                    .isNull();
            assertThat(profile.getRecordedQueries().get(1).getLazyLoadingTarget())
                    .isNotNull();
        }

        @Test
        void shouldSilentlyIgnoreLazyLoadingWhenNoTransactionIsActive() {
            // given.
            // no transaction has been started

            // when then.
            assertThatCode(() -> subject.recordLazyLoading(new LazyLoadingTarget(SampleEntity.class, "pets")))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldFailWhenNoQueryHasBeenRecordedInTheActiveTransaction() {
            // given.
            subject.recordNewTransactionStarted();

            // when then.
            assertThatThrownBy(() -> subject.recordLazyLoading(new LazyLoadingTarget(SampleEntity.class, "pets")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class TransactionLifecycle {

        @Test
        void shouldReturnCompletedProfileOnCompletion() {
            // given.
            subject.recordNewTransactionStarted();
            subject.recordSqlQuery(query("select 1"));

            // when.
            TransactionExecutionProfile profile = subject.recordTransactionCompletion();

            // then. A completed profile exposes its finish timestamp without throwing.
            assertThatCode(profile::getFinishedAtMillisFromEpoch).doesNotThrowAnyException();
            assertThat(profile.getQueriesCount()).isEqualTo(1);
        }

        @Test
        void shouldIsolateNestedRequiresNewTransactions() {
            // given.
            subject.recordNewTransactionStarted(); // outer
            subject.recordSqlQuery(query("outer-1"));

            subject.recordNewTransactionStarted(); // inner (REQUIRES_NEW)
            subject.recordSqlQuery(query("inner-1"));

            // when.
            TransactionExecutionProfile inner = subject.recordTransactionCompletion();
            subject.recordSqlQuery(query("outer-2"));
            TransactionExecutionProfile outer = subject.recordTransactionCompletion();

            // then.
            assertThat(inner.getRecordedQueries())
                    .extracting(AnalyzedSqlQueryRecord::getSql)
                    .containsExactly("inner-1");
            assertThat(outer.getRecordedQueries())
                    .extracting(AnalyzedSqlQueryRecord::getSql)
                    .containsExactly("outer-1", "outer-2");
        }

        @Test
        void shouldClearContextAfterOutermostTransactionCompletes() {
            // given.
            subject.recordNewTransactionStarted();
            subject.recordTransactionCompletion();

            // when then. Once the outermost transaction is completed, the stack is empty
            assertThatThrownBy(subject::recordTransactionCompletion).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldFailToCompleteWhenNoTransactionIsActive() {
            // given.
            // no transaction has been started

            // when then.
            assertThatThrownBy(subject::recordTransactionCompletion).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class ClearAll {

        @Test
        void shouldAllowStartingAFreshTransactionAfterClearing() {
            // given.
            subject.recordNewTransactionStarted();
            subject.recordSqlQuery(query("stale"));
            subject.clearAll();

            // when.
            subject.recordNewTransactionStarted();
            subject.recordSqlQuery(query("fresh"));
            TransactionExecutionProfile profile = subject.recordTransactionCompletion();

            // then.
            assertThat(profile.getRecordedQueries())
                    .extracting(AnalyzedSqlQueryRecord::getSql)
                    .containsExactly("fresh");
        }
    }

    private static class SampleEntity {}
}
