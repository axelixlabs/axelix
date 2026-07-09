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

import com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleSqlQueryRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for validating N+1 and Batch+1 query detection logic.
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

    @Autowired
    private N1CustomerRepository customerRepository;

    @Autowired
    private N1ProfileRepository profileRepository;

    @Autowired
    private N1SupplierRepository supplierRepository;

    @Autowired
    private N1WarehouseRepository warehouseRepository;

    @BeforeEach
    void setUp() {
        statsCollector.clearStats();
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        customerRepository.deleteAll();
        profileRepository.deleteAll();
        supplierRepository.deleteAll();
        warehouseRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // Collection N+1
    // -------------------------------------------------------------------------
    @Nested
    class CollectionNPlusOne {

        @Test
        void shouldDetectNPlusOne_forOneToManyLazy() {
            // given
            N1Order o1 = new N1Order();
            N1Order o2 = new N1Order();
            o1.addItem(new N1Item(o1));
            o2.addItem(new N1Item(o2));
            orderRepository.save(o1);
            orderRepository.save(o2);

            // when
            nPlusOneTestHelper.loadOrdersAndAccessItems();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersAndAccessItems");
            assertThat(queries).anyMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldDetectNPlusOne_forElementCollectionLazy() {
            // given
            orderRepository.save(new N1Order().addNote("note1"));
            orderRepository.save(new N1Order().addNote("note2"));

            // when
            nPlusOneTestHelper.loadOrdersAndAccessNotes();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersAndAccessNotes");
            assertThat(queries).anyMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldDetectNPlusOne_forManyToManyLazy() {
            // given
            N1Item i1 = new N1Item();
            i1.addCategory(new N1Category("c1"));
            N1Item i2 = new N1Item();
            i2.addCategory(new N1Category("c2"));
            itemRepository.save(i1);
            itemRepository.save(i2);

            // when
            nPlusOneTestHelper.loadItemsAndAccessCategories();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadItemsAndAccessCategories");
            assertThat(queries).anyMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }
    }

    // -------------------------------------------------------------------------
    // Collection Batch+1
    // -------------------------------------------------------------------------
    @Nested
    class CollectionBatchPlusOne {

        @Test
        void shouldDetectBatchPlusOne_forOneToManyWithBatchSize() {
            // given — 3 orders, batch size 2 → 2 batch selects
            orderRepository.save(new N1Order().addTag(new N1Tag("t1", null)));
            orderRepository.save(new N1Order().addTag(new N1Tag("t2", null)));
            orderRepository.save(new N1Order().addTag(new N1Tag("t3", null)));

            // when
            nPlusOneTestHelper.loadOrdersAndAccessTags();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersAndAccessTags");
            assertThat(queries).anyMatch(SqlQueryRecord::isBatchPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
        }

        @Test
        void shouldDetectBatchPlusOne_forElementCollectionWithBatchSize() {
            // given — 3 orders, batch size 2 → 2 batch selects
            orderRepository.save(new N1Order().addLabel("l1"));
            orderRepository.save(new N1Order().addLabel("l2"));
            orderRepository.save(new N1Order().addLabel("l3"));

            // when
            nPlusOneTestHelper.loadOrdersAndAccessLabels();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersAndAccessLabels");
            assertThat(queries).anyMatch(SqlQueryRecord::isBatchPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
        }

        @Test
        void shouldDetectBatchPlusOne_forManyToManyWithBatchSize() {
            // given — 3 items, batch size 2 → 2 batch selects
            N1Item i1 = new N1Item();
            i1.addLabel(new N1Label("l1"));
            N1Item i2 = new N1Item();
            i2.addLabel(new N1Label("l2"));
            N1Item i3 = new N1Item();
            i3.addLabel(new N1Label("l3"));
            itemRepository.save(i1);
            itemRepository.save(i2);
            itemRepository.save(i3);

            // when
            nPlusOneTestHelper.loadItemsAndAccessLabels();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadItemsAndAccessLabels");
            assertThat(queries).anyMatch(SqlQueryRecord::isBatchPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
        }
    }

    // -------------------------------------------------------------------------
    // Entity N+1
    // -------------------------------------------------------------------------
    @Nested
    class EntityNPlusOne {

        @Test
        void shouldDetectNPlusOne_forManyToOneLazy() {
            // given
            N1Customer customer1 = customerRepository.save(new N1Customer());
            N1Customer customer2 = customerRepository.save(new N1Customer());
            orderRepository.save(new N1Order().setCustomer(customer1));
            orderRepository.save(new N1Order().setCustomer(customer2));

            // when
            nPlusOneTestHelper.loadOrdersAndAccessCustomers();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersAndAccessCustomers");
            assertThat(queries).anyMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldDetectNPlusOne_forOneToOneOwningSideLazy() {
            // given
            customerRepository.save(new N1Customer().setProfile(new N1Profile("bio1")));
            customerRepository.save(new N1Customer().setProfile(new N1Profile("bio2")));

            // when
            nPlusOneTestHelper.loadCustomersAndAccessProfiles();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadCustomersAndAccessProfiles");
            assertThat(queries).anyMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldDetectNPlusOne_forManyToOneEager() {
            // given — EAGER fetch without JOIN still causes N+1
            // Hibernate issues separate SELECT per warehouse instead of JOIN
            N1Warehouse w1 = warehouseRepository.save(new N1Warehouse("loc1"));
            N1Warehouse w2 = warehouseRepository.save(new N1Warehouse("loc2"));
            orderRepository.save(new N1Order().setWarehouse(w1));
            orderRepository.save(new N1Order().setWarehouse(w2));

            // when
            nPlusOneTestHelper.loadOrdersEager();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersEager");
            assertThat(queries).anyMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }
    }

    // -------------------------------------------------------------------------
    // Entity Batch+1
    // -------------------------------------------------------------------------
    @Nested
    class EntityBatchPlusOne {

        @Test
        void shouldDetectBatchPlusOne_forOneToOneOwningSideWithBatchSize() {
            // given — 3 customers with addresses, @BatchSize(2) on N1Address
            // → 2 batch selects instead of 3 individual selects
            customerRepository.save(new N1Customer().setAddress(new N1Address("city1")));
            customerRepository.save(new N1Customer().setAddress(new N1Address("city2")));
            customerRepository.save(new N1Customer().setAddress(new N1Address("city3")));

            // when
            nPlusOneTestHelper.loadCustomersAndAccessAddresses();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadCustomersAndAccessAddresses");
            assertThat(queries).anyMatch(SqlQueryRecord::isBatchPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
        }

        @Test
        void shouldDetectBatchPlusOne_forManyToOneWithBatchSize() {
            // given — 3 orders with different suppliers, @BatchSize(2) on N1Supplier
            // → 2 batch selects instead of 3 individual selects
            N1Supplier s1 = supplierRepository.save(new N1Supplier("s1"));
            N1Supplier s2 = supplierRepository.save(new N1Supplier("s2"));
            N1Supplier s3 = supplierRepository.save(new N1Supplier("s3"));
            orderRepository.save(new N1Order().setSupplier(s1));
            orderRepository.save(new N1Order().setSupplier(s2));
            orderRepository.save(new N1Order().setSupplier(s3));

            // when
            nPlusOneTestHelper.loadOrdersAndAccessSuppliers();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersAndAccessSuppliers");
            assertThat(queries).anyMatch(SqlQueryRecord::isBatchPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
        }
    }

    // -------------------------------------------------------------------------
    // Not tracked
    // -------------------------------------------------------------------------
    @Nested
    class NotTracked {

        /**
         * Verifies that the N+1 detection mechanism does not track the inverse side (mappedBy)
         * of a {@code @OneToOne} association.
         * <p>
         * Hibernate resolves the inverse side of a one-to-one relationship using a secondary
         * SELECT query (e.g., {@code WHERE fk_column = ?}). This internal process completely
         * bypasses {@code EventType.LOAD} (utilizing {@code OneToOneType.resolve()} internally).
         * <p>
         * This is a recognized limitation of the current monitoring system. Addressing this
         * edge case would require highly complex structural changes to the core architecture,
         * and it remains uncertain whether supporting this specific pattern is generally necessary.
         */
        @Test
        void shouldNotDetect_forOneToOneInverseSide() {
            // given
            customerRepository.save(new N1Customer().setProfile(new N1Profile("bio1")));
            customerRepository.save(new N1Customer().setProfile(new N1Profile("bio2")));

            // when
            nPlusOneTestHelper.loadProfilesAndAccessCustomers();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadProfilesAndAccessCustomers");
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldNotDetect_forSubselect() {
            // given
            N1Order o1 = new N1Order();
            o1.addComment(new N1Comment("c1", o1));
            N1Order o2 = new N1Order();
            o2.addComment(new N1Comment("c2", o2));
            orderRepository.save(o1);
            orderRepository.save(o2);

            // when
            nPlusOneTestHelper.loadOrdersAndAccessCommentsSubselect();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersAndAccessCommentsSubselect");
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldNotDetect_forJoinFetch() {
            // given
            N1Order o1 = new N1Order();
            o1.addItem(new N1Item(o1));
            N1Order o2 = new N1Order();
            o2.addItem(new N1Item(o2));
            orderRepository.save(o1);
            orderRepository.save(o2);

            // when
            nPlusOneTestHelper.loadOrdersWithItemsJoinFetch();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersWithItemsJoinFetch");
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldNotDetect_forEntityGraph() {
            // given
            N1Order o1 = new N1Order();
            o1.addItem(new N1Item(o1));
            N1Order o2 = new N1Order();
            o2.addItem(new N1Item(o2));
            orderRepository.save(o1);
            orderRepository.save(o2);

            // when
            nPlusOneTestHelper.loadOrdersWithItemsEntityGraph();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersWithItemsEntityGraph");
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        // -------------------------------------------------------------------------
        // Batch not tracked
        // -------------------------------------------------------------------------
        @Test
        void shouldNotDetect_forOneToManyWhenBatchSizeCoverAllOwners() {
            // given — batch size is 2, only 2 orders → fits in one batch → no Batch+1
            orderRepository.save(new N1Order().addTag(new N1Tag("t1", null)));
            orderRepository.save(new N1Order().addTag(new N1Tag("t2", null)));

            // when
            nPlusOneTestHelper.loadOrdersAndAccessTags();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersAndAccessTags");
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldNotDetect_forElementCollectionWhenBatchSizeCoversAllOwners() {
            // given — batch size is 2, only 2 orders → fits in one batch → no Batch+1
            orderRepository.save(new N1Order().addLabel("l1"));
            orderRepository.save(new N1Order().addLabel("l2"));

            // when
            nPlusOneTestHelper.loadOrdersAndAccessLabels();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersAndAccessLabels");
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldNotDetect_forManyToManyWhenBatchSizeCoversAllItems() {
            // given — batch size is 2, only 2 items → fits in one batch → no Batch+1
            N1Item i1 = new N1Item();
            i1.addLabel(new N1Label("l1"));
            N1Item i2 = new N1Item();
            i2.addLabel(new N1Label("l2"));
            itemRepository.save(i1);
            itemRepository.save(i2);

            // when
            nPlusOneTestHelper.loadItemsAndAccessLabels();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadItemsAndAccessLabels");
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldNotDetect_forEntityWhenBatchSizeCoversAllEntities() {
            // given — @BatchSize(2) on N1Supplier, only 2 suppliers → fits in one batch → no Batch+1
            N1Supplier s1 = supplierRepository.save(new N1Supplier("s1"));
            N1Supplier s2 = supplierRepository.save(new N1Supplier("s2"));
            orderRepository.save(new N1Order().setSupplier(s1));
            orderRepository.save(new N1Order().setSupplier(s2));

            // when
            nPlusOneTestHelper.loadOrdersAndAccessSuppliers();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadOrdersAndAccessSuppliers");
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }

        @Test
        void shouldNotDetect_forOneToOneOwningSideWhenBatchSizeCoversAll() {
            // given — @BatchSize(2) on N1Address, only 2 customers → fits in one batch
            customerRepository.save(new N1Customer().setAddress(new N1Address("city1")));
            customerRepository.save(new N1Customer().setAddress(new N1Address("city2")));

            // when
            nPlusOneTestHelper.loadCustomersAndAccessAddresses();

            // then
            List<SimpleSqlQueryRecord> queries = getQueriesFor("loadCustomersAndAccessAddresses");
            assertThat(queries).noneMatch(SqlQueryRecord::isNPlusOne);
            assertThat(queries).noneMatch(SqlQueryRecord::isBatchPlusOne);
        }
    }

    private List<SimpleSqlQueryRecord> getQueriesFor(String methodName) {
        return statsCollector.getAllStats().entrySet().stream()
                .filter(e -> e.getKey().getMethod().getName().equals(methodName))
                .flatMap(e -> e.getValue().get().stream())
                .flatMap(r -> r.getQueries().stream())
                .collect(Collectors.toList());
    }
}
