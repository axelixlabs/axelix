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
package com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.transactions.SqlQueryRecord;
import com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate.NPlusOneHolder.NPlusOneContext;

import static org.assertj.core.api.Assertions.assertThat;

/** * Unit tests for {@link NPlusOneAnalyzer}.
 *
 * @author Nikita Kirillov
 */
class NPlusOneAnalyzerTest {

    private final NPlusOneAnalyzer analyzer = new NPlusOneAnalyzer();

    @Test
    void shouldNotMarkQuery_whenSingleCollectionLoad() {
        // given
        NPlusOneContext context = new NPlusOneContext();

        context.getCollectionLoadsSnapshot().record("Order.items", 1, 1, false);

        SqlQueryRecord query = queryWithIndex(1);
        List<SqlQueryRecord> queries = List.of(query);

        // when
        analyzer.analyzeAndMarkNPlusOneQueries(context, queries);

        // then
        assertThat(query.isNPlusOne()).isFalse();
        assertThat(query.isBatchPlusOne()).isFalse();
    }

    @Test
    void shouldMarkQueries_whenMultipleCollectionLoads() {
        // given
        NPlusOneContext context = new NPlusOneContext();
        context.getCollectionLoadsSnapshot().record("Order.items", 1, 1, false);
        context.getCollectionLoadsSnapshot().record("Order.items", 2, 2, false);
        context.getCollectionLoadsSnapshot().record("Order.items", 3, 3, false);

        SqlQueryRecord query1 = queryWithIndex(1);
        SqlQueryRecord query2 = queryWithIndex(2);
        SqlQueryRecord query3 = queryWithIndex(3);
        SqlQueryRecord unrelatedQuery = queryWithIndex(4);
        List<SqlQueryRecord> queries = List.of(query1, query2, query3, unrelatedQuery);

        // when
        analyzer.analyzeAndMarkNPlusOneQueries(context, queries);

        // then
        assertThat(query1.isNPlusOne()).isTrue();
        assertThat(query2.isNPlusOne()).isTrue();
        assertThat(query3.isNPlusOne()).isTrue();
        assertThat(query1.isBatchPlusOne()).isFalse();
        assertThat(query2.isBatchPlusOne()).isFalse();
        assertThat(query3.isBatchPlusOne()).isFalse();

        assertThat(unrelatedQuery.isNPlusOne()).isFalse();
        assertThat(unrelatedQuery.isBatchPlusOne()).isFalse();
    }

    @Test
    void shouldMarkAsBatchPlusOne_whenMultipleBatchCollectionLoads() {
        // given
        NPlusOneContext context = new NPlusOneContext();
        context.getCollectionLoadsSnapshot().record("Order.items", 1, 1, true);
        context.getCollectionLoadsSnapshot().record("Order.items", 2, 2, true);

        SqlQueryRecord query1 = queryWithIndex(1);
        SqlQueryRecord query2 = queryWithIndex(2);
        List<SqlQueryRecord> queries = List.of(query1, query2);

        // when
        analyzer.analyzeAndMarkNPlusOneQueries(context, queries);

        // then
        assertThat(query1.isBatchPlusOne()).isTrue();
        assertThat(query2.isBatchPlusOne()).isTrue();
        assertThat(query1.isNPlusOne()).isFalse();
        assertThat(query2.isNPlusOne()).isFalse();
    }

    @Test
    void shouldHandleMultipleRoles_independently() {
        // given
        NPlusOneContext context = new NPlusOneContext();
        // N+1 on first role
        context.getCollectionLoadsSnapshot().record("Order.items", 1, 1, false);
        context.getCollectionLoadsSnapshot().record("Order.items", 2, 2, false);
        // only one load, not N+1
        context.getCollectionLoadsSnapshot().record("Order.tags", 1, 3, false);

        SqlQueryRecord query1 = queryWithIndex(1);
        SqlQueryRecord query2 = queryWithIndex(2);
        SqlQueryRecord query3 = queryWithIndex(3);
        List<SqlQueryRecord> queries = List.of(query1, query2, query3);

        // when
        analyzer.analyzeAndMarkNPlusOneQueries(context, queries);

        // then
        assertThat(query1.isNPlusOne()).isTrue();
        assertThat(query2.isNPlusOne()).isTrue();
        assertThat(query3.isNPlusOne()).isFalse();
    }

    @Test
    void shouldNotMarkQuery_whenSingleEntityLoad() {
        // given
        NPlusOneContext context = new NPlusOneContext();
        context.getEntityLoadsSnapshot().record("Item", 1, 1, false);

        SqlQueryRecord query = queryWithIndex(1);
        List<SqlQueryRecord> queries = List.of(query);

        // when
        analyzer.analyzeAndMarkNPlusOneQueries(context, queries);

        // then
        assertThat(query.isNPlusOne()).isFalse();
        assertThat(query.isBatchPlusOne()).isFalse();
    }

    @Test
    void shouldMarkQueries_whenMultipleEntityLoads() {
        // given
        NPlusOneContext context = new NPlusOneContext();
        context.getEntityLoadsSnapshot().record("Item", 1, 1, false);
        context.getEntityLoadsSnapshot().record("Item", 2, 2, false);
        context.getEntityLoadsSnapshot().record("Item", 3, 3, false);

        SqlQueryRecord query1 = queryWithIndex(1);
        SqlQueryRecord query2 = queryWithIndex(2);
        SqlQueryRecord query3 = queryWithIndex(3);
        List<SqlQueryRecord> queries = List.of(query1, query2, query3);

        // when
        analyzer.analyzeAndMarkNPlusOneQueries(context, queries);

        // then
        assertThat(query1.isNPlusOne()).isTrue();
        assertThat(query2.isNPlusOne()).isTrue();
        assertThat(query3.isNPlusOne()).isTrue();
        assertThat(query1.isBatchPlusOne()).isFalse();
        assertThat(query2.isBatchPlusOne()).isFalse();
        assertThat(query3.isBatchPlusOne()).isFalse();
    }

    @Test
    void shouldMarkAsBatchPlusOne_whenMultipleBatchEntityLoads() {
        // given
        NPlusOneContext context = new NPlusOneContext();
        context.getEntityLoadsSnapshot().record("Item", 1, 1, true);
        context.getEntityLoadsSnapshot().record("Item", 2, 2, true);

        SqlQueryRecord query1 = queryWithIndex(1);
        SqlQueryRecord query2 = queryWithIndex(2);
        List<SqlQueryRecord> queries = List.of(query1, query2);

        // when
        analyzer.analyzeAndMarkNPlusOneQueries(context, queries);

        // then
        assertThat(query1.isBatchPlusOne()).isTrue();
        assertThat(query2.isBatchPlusOne()).isTrue();
        assertThat(query1.isNPlusOne()).isFalse();
        assertThat(query2.isNPlusOne()).isFalse();
    }

    @Test
    void shouldNotMarkAnything_whenContextIsEmpty() {
        // given
        NPlusOneContext context = new NPlusOneContext();
        SqlQueryRecord query = queryWithIndex(1);
        List<SqlQueryRecord> queries = List.of(query);

        // when
        analyzer.analyzeAndMarkNPlusOneQueries(context, queries);

        // then
        assertThat(query.isNPlusOne()).isFalse();
        assertThat(query.isBatchPlusOne()).isFalse();
    }

    @Test
    void shouldNotMarkAnything_whenQueriesListIsEmpty() {
        // given
        NPlusOneContext context = new NPlusOneContext();
        context.getCollectionLoadsSnapshot().record("Order.items", 1, 1, false);
        context.getCollectionLoadsSnapshot().record("Order.items", 2, 2, false);

        // when + then — no exception
        analyzer.analyzeAndMarkNPlusOneQueries(context, List.of());
    }

    private SqlQueryRecord queryWithIndex(int sqlIndex) {
        return new SqlQueryRecord("select 1", 0L, 0L, false, sqlIndex);
    }
}
