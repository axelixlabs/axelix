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
package com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionExecutionProfile.AnalyzedSqlQueryRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for validating N+1 and Batch+1 query detection logic for collection lazy loading.
 *
 * @author Nikita Kirillov
 */
class NPlusOneDetectionTest extends AbstractTransactionMonitoringSharedContextTest {

    @Autowired
    private NPlusOneTestHelper nPlusOneTestHelper;

    @Autowired
    private TransactionStatsCollector statsCollector;

    @Autowired
    private N1OrderRepository orderRepository;

    @Autowired
    private N1ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        statsCollector.clearStats();
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Nested
    class CollectionNPlusOne {

        @Test
        void shouldDetectNPlusOne_forOneToManyLazy() {
            // given.
            N1Order o1 = new N1Order();
            N1Order o2 = new N1Order();
            o1.addItem(new N1Item(o1));
            o2.addItem(new N1Item(o2));
            orderRepository.save(o1);
            orderRepository.save(o2);

            // when.
            nPlusOneTestHelper.loadOrdersAndAccessItems();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries =
                    lazyLoadedQueriesFor("loadOrdersAndAccessItems", "n1Items");
            assertThat(lazyLoadedQueries).hasSize(2);
        }

        @Test
        void shouldDetectNPlusOne_forElementCollectionLazy() {
            // given.
            orderRepository.save(new N1Order().addNote("note1"));
            orderRepository.save(new N1Order().addNote("note2"));

            // when.
            nPlusOneTestHelper.loadOrdersAndAccessNotes();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries = lazyLoadedQueriesFor("loadOrdersAndAccessNotes", "notes");
            assertThat(lazyLoadedQueries).hasSize(2);
        }

        @Test
        void shouldDetectNPlusOne_forManyToManyLazy() {
            // given.
            N1Item i1 = new N1Item();
            i1.addCategory(new N1Category("c1"));
            N1Item i2 = new N1Item();
            i2.addCategory(new N1Category("c2"));
            itemRepository.save(i1);
            itemRepository.save(i2);

            // when.
            nPlusOneTestHelper.loadItemsAndAccessCategories();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries =
                    lazyLoadedQueriesFor("loadItemsAndAccessCategories", "categories");
            assertThat(lazyLoadedQueries).hasSize(2);
        }
    }

    @Nested
    class CollectionBatchPlusOne {

        @Test
        void shouldDetectBatchPlusOne_forOneToManyWithBatchSize() {
            // given — 3 orders, batch size 2 → 2 batch selects.
            orderRepository.save(new N1Order().addTag(new N1Tag("t1", null)));
            orderRepository.save(new N1Order().addTag(new N1Tag("t2", null)));
            orderRepository.save(new N1Order().addTag(new N1Tag("t3", null)));

            // when.
            nPlusOneTestHelper.loadOrdersAndAccessTags();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries = lazyLoadedQueriesFor("loadOrdersAndAccessTags", "tags");
            assertThat(lazyLoadedQueries).hasSize(2);
        }

        @Test
        void shouldDetectBatchPlusOne_forElementCollectionWithBatchSize() {
            // given — 3 orders, batch size 2 → 2 batch selects.
            orderRepository.save(new N1Order().addLabel("l1"));
            orderRepository.save(new N1Order().addLabel("l2"));
            orderRepository.save(new N1Order().addLabel("l3"));

            // when.
            nPlusOneTestHelper.loadOrdersAndAccessLabels();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries =
                    lazyLoadedQueriesFor("loadOrdersAndAccessLabels", "labels");
            assertThat(lazyLoadedQueries).hasSize(2);
        }

        @Test
        void shouldDetectBatchPlusOne_forManyToManyWithBatchSize() {
            // given — 3 items, batch size 2 → 2 batch selects.
            N1Item i1 = new N1Item();
            i1.addLabel(new N1Label("l1"));
            N1Item i2 = new N1Item();
            i2.addLabel(new N1Label("l2"));
            N1Item i3 = new N1Item();
            i3.addLabel(new N1Label("l3"));
            itemRepository.save(i1);
            itemRepository.save(i2);
            itemRepository.save(i3);

            // when.
            nPlusOneTestHelper.loadItemsAndAccessLabels();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries = lazyLoadedQueriesFor("loadItemsAndAccessLabels", "labels");
            assertThat(lazyLoadedQueries).hasSize(2);
        }
    }

    @Nested
    class NotTracked {

        @Test
        void shouldNotDetect_forSubselect() {
            // given.
            N1Order o1 = new N1Order();
            o1.addComment(new N1Comment("c1", o1));
            N1Order o2 = new N1Order();
            o2.addComment(new N1Comment("c2", o2));
            orderRepository.save(o1);
            orderRepository.save(o2);

            // when.
            nPlusOneTestHelper.loadOrdersAndAccessCommentsSubselect();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries =
                    lazyLoadedQueriesFor("loadOrdersAndAccessCommentsSubselect", "comments");
            assertThat(lazyLoadedQueries).hasSizeLessThanOrEqualTo(1);
        }

        @Test
        void shouldNotDetect_forJoinFetch() {
            // given.
            N1Order o1 = new N1Order();
            o1.addItem(new N1Item(o1));
            N1Order o2 = new N1Order();
            o2.addItem(new N1Item(o2));
            orderRepository.save(o1);
            orderRepository.save(o2);

            // when.
            nPlusOneTestHelper.loadOrdersWithItemsJoinFetch();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries =
                    lazyLoadedQueriesFor("loadOrdersWithItemsJoinFetch", "n1Items");
            assertThat(lazyLoadedQueries).isEmpty();
        }

        @Test
        void shouldNotDetect_forEntityGraph() {
            // given.
            N1Order o1 = new N1Order();
            o1.addItem(new N1Item(o1));
            N1Order o2 = new N1Order();
            o2.addItem(new N1Item(o2));
            orderRepository.save(o1);
            orderRepository.save(o2);

            // when.
            nPlusOneTestHelper.loadOrdersWithItemsEntityGraph();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries =
                    lazyLoadedQueriesFor("loadOrdersWithItemsEntityGraph", "n1Items");
            assertThat(lazyLoadedQueries).isEmpty();
        }

        @Test
        void shouldNotDetect_forOneToManyWhenBatchSizeCoversAllOwners() {
            // given — batch size is 2, only 2 orders → fits in one batch.
            orderRepository.save(new N1Order().addTag(new N1Tag("t1", null)));
            orderRepository.save(new N1Order().addTag(new N1Tag("t2", null)));

            // when.
            nPlusOneTestHelper.loadOrdersAndAccessTags();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries = lazyLoadedQueriesFor("loadOrdersAndAccessTags", "tags");
            assertThat(lazyLoadedQueries).hasSize(1);
        }

        @Test
        void shouldNotDetect_forElementCollectionWhenBatchSizeCoversAllOwners() {
            // given — batch size is 2, only 2 orders → fits in one batch.
            orderRepository.save(new N1Order().addLabel("l1"));
            orderRepository.save(new N1Order().addLabel("l2"));

            // when.
            nPlusOneTestHelper.loadOrdersAndAccessLabels();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries =
                    lazyLoadedQueriesFor("loadOrdersAndAccessLabels", "labels");
            assertThat(lazyLoadedQueries).hasSize(1);
        }

        @Test
        void shouldNotDetect_forManyToManyWhenBatchSizeCoversAllItems() {
            // given — batch size is 2, only 2 items → fits in one batch.
            N1Item i1 = new N1Item();
            i1.addLabel(new N1Label("l1"));
            N1Item i2 = new N1Item();
            i2.addLabel(new N1Label("l2"));
            itemRepository.save(i1);
            itemRepository.save(i2);

            // when.
            nPlusOneTestHelper.loadItemsAndAccessLabels();

            // then.
            List<AnalyzedSqlQueryRecord> lazyLoadedQueries = lazyLoadedQueriesFor("loadItemsAndAccessLabels", "labels");
            assertThat(lazyLoadedQueries).hasSize(1);
        }
    }

    private List<AnalyzedSqlQueryRecord> getQueriesFor(String methodName) {
        return statsCollector.getAllStats().entrySet().stream()
                .filter(e -> e.getKey().getMethod().getName().equals(methodName))
                .flatMap(e -> e.getValue().get().stream())
                .flatMap(profile -> profile.getRecordedQueries().stream())
                .collect(Collectors.toList());
    }

    private List<AnalyzedSqlQueryRecord> lazyLoadedQueriesFor(String methodName, String associationPropertyName) {
        return getQueriesFor(methodName).stream()
                .filter(query -> query.getLazyLoadingTarget() != null)
                .filter(query -> associationPropertyName.equals(
                        query.getLazyLoadingTarget().associationPropertyName()))
                .collect(Collectors.toList());
    }
}
