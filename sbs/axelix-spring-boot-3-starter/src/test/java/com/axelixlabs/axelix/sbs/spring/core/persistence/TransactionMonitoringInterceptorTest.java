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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LazyLoadingTarget;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStats;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for {@link TransactionMonitoringInterceptor}. Aims to test the behavior
 * of Axelix detecting various problems during persistence, such as N + 1 and so on.
 *
 * @author Mikhail Polivakha
 */
class TransactionMonitoringInterceptorTest extends AbstractTransactionMonitoringSharedContextTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private TransactionStatsCollector transactionStatsCollector;

    @BeforeEach
    void setUp() {
        petRepository.deleteAll();
        ownerRepository.deleteAll();

        Owner first = new Owner().setLastName("Davis");
        first.addPet(new Pet("Basil", first));
        first.addTag(new Tag("friendly", first));

        Owner second = new Owner().setLastName("Carter");
        second.addPet(new Pet("Leo", second));
        second.addTag(new Tag("trained", second));

        ownerRepository.saveAll(List.of(first, second));
        transactionStatsCollector.clear();
    }

    @Nested
    class SimpleQueries {

        @Test
        void shouldRecordTransactionWithMultipleSimpleQueries() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeMultipleSimpleQueries", String.class);

            // when.
            ownerRepository.executeMultipleSimpleQueries("Evans");

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).isEmpty();
            assertThat(stats.getInMemoryPaginatedEntities()).isEmpty();
        }
    }

    @Nested
    class NPlusOne {

        @Test
        void shouldRecordTransactionWithOneNPlusOne() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeNPlusOneOnly");
            LazyLoadingTarget pets = new LazyLoadingTarget(Owner.class, "pets");

            // when.
            ownerRepository.executeNPlusOneOnly();

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).containsEntry(pets, 2); // two lazy loadings of pets
            assertThat(stats.getInMemoryPaginatedEntities()).isEmpty();
        }

        @Test
        void shouldRecordTransactionWithOneNPlusOneAndSimpleQuery() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeNPlusOneAndSimpleQuery", String.class);
            LazyLoadingTarget pets = new LazyLoadingTarget(Owner.class, "pets");

            // when.
            ownerRepository.executeNPlusOneAndSimpleQuery("Davis");

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).containsEntry(pets, 2);
            assertThat(stats.getInMemoryPaginatedEntities()).isEmpty();
        }

        @Test
        void shouldRecordTransactionWithTwoNPlusOnesAndSimpleQuery() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeTwoNPlusOnesAndSimpleQuery", String.class);
            LazyLoadingTarget pets = new LazyLoadingTarget(Owner.class, "pets");
            LazyLoadingTarget tags = new LazyLoadingTarget(Owner.class, "tags");

            // when.
            ownerRepository.executeTwoNPlusOnesAndSimpleQuery("Davis");

            // then.
            TransactionStats stats = statsFor(key);

            // Tags use @BatchSize, so they are loaded by a single batch load.
            assertThat(stats.getNPlusOneOccasions()).containsEntry(pets, 2).containsEntry(tags, 1);
            assertThat(stats.getInMemoryPaginatedEntities()).isEmpty();
        }
    }

    @Nested
    class InMemoryPagination {

        @Test
        void shouldRecordTransactionWithInMemoryPagination() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeInMemoryPagination");

            // when.
            ownerRepository.executeInMemoryPagination();

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).isEmpty();
            assertThat(stats.getInMemoryPaginatedEntities()).hasSize(1).containsEntry("owner", 1);
        }

        @Test
        void shouldRecordTransactionWithInMemoryPaginationAndSimpleQuery() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeInMemoryPaginationAndSimpleQuery", String.class);

            // when.
            ownerRepository.executeInMemoryPaginationAndSimpleQuery("Davis");

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).isEmpty();
            assertThat(stats.getInMemoryPaginatedEntities()).hasSize(1).containsEntry("owner", 1);
        }

        @Test
        void shouldRecordTransactionWithInMemoryPaginationNPlusOneAndSimpleQuery() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeInMemoryPaginationNPlusOneAndSimpleQuery", String.class);
            LazyLoadingTarget pets = new LazyLoadingTarget(Owner.class, "pets");

            // when.
            ownerRepository.executeInMemoryPaginationNPlusOneAndSimpleQuery("Davis");

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).containsKey(pets);
            assertThat(stats.getInMemoryPaginatedEntities()).hasSize(1).containsEntry("owner", 1);
        }
    }

    private TransactionStats statsFor(MethodClassKey key) {
        Map<MethodClassKey, TransactionStats> stats = transactionStatsCollector.getCopyOfStats();
        assertThat(stats).containsKey(key);
        return stats.get(key);
    }

    private static MethodClassKey keyFor(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = OwnerRepository.class.getMethod(methodName, parameterTypes);
        return new MethodClassKey(method, OwnerRepository.class);
    }
}
