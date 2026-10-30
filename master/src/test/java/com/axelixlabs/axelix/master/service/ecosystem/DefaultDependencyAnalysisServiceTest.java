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
package com.axelixlabs.axelix.master.service.ecosystem;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.DependencyLibrariesResponse;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.domain.instance.InstanceSbom;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.CycloneDxDocuments;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Integration tests for {@link DefaultDependencyAnalysisService}.
 *
 * @author Nikita Kirillov
 */
@DatabaseMatrixTest
class DefaultDependencyAnalysisServiceTest {

    private static final ApplicationId APPLICATION_ID = ApplicationId.of("com.axelixlabs.test", "app");

    @Autowired
    private DependencyAnalysisService subject;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private Environment environment;

    @Autowired
    private InstanceRegistry instanceRegistry;

    @BeforeEach
    void setUp() {
        jdbcAggregateTemplate.deleteAll(ApplicationDependencies.class);
        jdbcAggregateTemplate.deleteAll(Instance.class);
    }

    @Test
    void shouldReplaceTheSbomOfTheInstanceAndOfTheApplicationOnEveryStore() {
        // given.
        Instance instance = registeredInstanceOf(APPLICATION_ID);
        subject.store(instance, CycloneDxDocuments.of("com.axelixlabs.test:app:1.0.0", "org.slf4j:slf4j-api:2.0.9"));

        // when. the same instance reports a newer SBOM.
        String newer = CycloneDxDocuments.of("com.axelixlabs.test:app:1.1.0", "org.slf4j:slf4j-api:2.0.13");
        subject.store(instance, newer);

        // then.
        assertThat(jdbcAggregateTemplate.findAll(ApplicationDependencies.class))
                .singleElement()
                .extracting(ApplicationDependencies::sbom)
                .isEqualTo(newer);
        assertThat(jdbcAggregateTemplate.findAll(InstanceSbom.class))
                .singleElement()
                .extracting(InstanceSbom::sbom)
                .isEqualTo(newer);
    }

    @Test
    void shouldDropTheSbomOfTheInstanceTogetherWithTheInstance() {
        // given.
        Instance instance = registeredInstanceOf(APPLICATION_ID);
        subject.store(instance, CycloneDxDocuments.of("com.axelixlabs.test:app:1.0.0", "org.slf4j:slf4j-api:2.0.9"));

        // when.
        instanceRegistry.deRegister(instance.id());

        // then.
        assertThat(jdbcAggregateTemplate.findAll(InstanceSbom.class)).isEmpty();
    }

//    @Test
    void shouldKeepTheTransactionUsableWhenAnotherInstanceStoresTheSbomConcurrently() throws Exception {
        // SQLite does not let two transactions write concurrently, so the race ends with a lock error there
        assumeFalse(environment.getRequiredProperty("spring.datasource.url").startsWith("jdbc:sqlite"));

        Instance first = registeredInstanceOf(APPLICATION_ID);
        Instance second = registeredInstanceOf(APPLICATION_ID);
        Instance otherInstance = registeredInstanceOf(ApplicationId.of("com.axelixlabs.test", "other"));

        // given. the first instance of a new application inserts its SBOM, but does not commit yet.
        String firstApplicationSbom =
                CycloneDxDocuments.of("com.axelixlabs.test:app:1.0.0", "org.slf4j:slf4j-api:2.0.9");
        CountDownLatch firstStored = new CountDownLatch(1);
        CountDownLatch commitFirst = new CountDownLatch(1);
        CompletableFuture<Void> firstInstance =
                CompletableFuture.runAsync(() -> transactionTemplate.executeWithoutResult(_ -> {
                    subject.store(first, firstApplicationSbom);
                    firstStored.countDown();
                    await(commitFirst);
                }));
        firstStored.await();

        // when. the second instance does not see that row, so its insert waits for the first commit and then
        // hits the unique index, after which it keeps working in the same transaction.
        String secondApplicationSbom =
                CycloneDxDocuments.of("com.axelixlabs.test:app:1.0.0", "org.slf4j:slf4j-api:2.0.13");
        String otherApplicationSbom =
                CycloneDxDocuments.of("com.axelixlabs.test:other:1.0.0", "org.slf4j:slf4j-api:2.0.9");
        CompletableFuture<Void> secondInstance =
                CompletableFuture.runAsync(() -> transactionTemplate.executeWithoutResult(_ -> {
                    subject.store(second, secondApplicationSbom);
                    // the transaction is still usable after the failed insert above
                    subject.store(otherInstance, otherApplicationSbom);
                }));
        Thread.sleep(1000);
        commitFirst.countDown();
        firstInstance.get();
        secondInstance.get();

        // then. the first SBOM is kept, and the rest of the second transaction is committed.
        assertThat(jdbcAggregateTemplate.findAll(ApplicationDependencies.class))
                .extracting(ApplicationDependencies::sbom)
                .doesNotContain(secondApplicationSbom)
                .containsExactlyInAnyOrder(firstApplicationSbom, otherApplicationSbom);
    }

    @Test
    void shouldKeepThePreviousSbomWhenTheNewOneIsMalformed() {
        // given.
        String previous = CycloneDxDocuments.of("com.axelixlabs.test:app:1.0.0", "org.slf4j:slf4j-api:2.0.9");
        subject.store(registeredInstanceOf(APPLICATION_ID), previous);

        // when.
        subject.store(registeredInstanceOf(APPLICATION_ID), "{ \"bomFormat\": \"CycloneDX\"");

        // then.
        assertThat(jdbcAggregateTemplate.findAll(ApplicationDependencies.class))
                .singleElement()
                .extracting(ApplicationDependencies::sbom)
                .isEqualTo(previous);
    }

    @Test
    void shouldStoreAnSbomLargerThanAPlainTextColumnHolds() {
        // given. MySQL caps TEXT at 64 KiB, which a large application can outgrow.
        String sbom = CycloneDxDocuments.of(
                "com.axelixlabs.test:app:1.0.0",
                IntStream.range(0, 500)
                        .mapToObj(index -> "com.example.library:artifact-number-" + index + ":1.0.0")
                        .toArray(String[]::new));

        // when.
        subject.store(registeredInstanceOf(APPLICATION_ID), sbom);

        // then.
        assertThat(sbom.length()).isGreaterThan(64 * 1024);
        assertThat(jdbcAggregateTemplate.findAll(ApplicationDependencies.class))
                .singleElement()
                .extracting(ApplicationDependencies::sbom)
                .isEqualTo(sbom);
    }

    @Test
    void shouldReturnAnEmptyDashboardWhenNoApplicationReportedItsSbom() {
        // when.
        DependenciesDashboardResponse dashboard = subject.getDashboard();

        // then.
        assertThat(dashboard.totalApplications()).isZero();
        assertThat(dashboard.applicationsWithRiskyDependencies()).isZero();
        assertThat(dashboard.applicationsByStatus()).isEmpty();
        assertThat(dashboard.totalLibraries()).isZero();
        assertThat(dashboard.librariesByStatus()).isEmpty();
        assertThat(dashboard.riskyLibrariesByEcosystem()).isEmpty();
    }

    @Test
    void shouldSkipAnUnparsableStoredSbomOnTheDashboard() {
        // given. a stored SBOM that no longer parses, e.g. one written before a parser change.
        jdbcAggregateTemplate.insert(new ApplicationDependencies(
                UUID.randomUUID().toString(),
                ApplicationId.of("com.axelixlabs.test", "broken"),
                "{ \"bomFormat\": \"CycloneDX\"",
                Instant.now()));
        subject.store(
                registeredInstanceOf(APPLICATION_ID),
                CycloneDxDocuments.of("com.axelixlabs.test:app:1.0.0", "org.hdrhistogram:HdrHistogram:2.2.2"));

        // when.
        DependenciesDashboardResponse dashboard = subject.getDashboard();
        DependencyLibrariesResponse libraries =
                subject.getLibraries(new DependencyLibrariesQuery(null, Set.of(), Set.of(), 0, 10));

        // then. the dashboard is built from the remaining SBOMs only.
        assertThat(dashboard.totalApplications()).isEqualTo(1);
        assertThat(dashboard.applicationsWithRiskyDependencies()).isEqualTo(1);
        assertThat(libraries.libraries()).singleElement().satisfies(library -> {
            assertThat(library.applicationCount()).isEqualTo(1);
            assertThat(library.share()).isEqualTo(100.0);
        });
    }

    private Instance registeredInstanceOf(ApplicationId applicationId) {
        Instance instance = TestInstanceFactory.create(
                UUID.randomUUID().toString(), applicationId.groupId(), applicationId.artifactId());
        instanceRegistry.register(instance);
        return instance;
    }

    private static void await(CountDownLatch countDownLatch) {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
