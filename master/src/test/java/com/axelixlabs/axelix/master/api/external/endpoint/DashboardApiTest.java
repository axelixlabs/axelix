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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.contract.metadata.CountedLazyLoadingTarget;
import com.axelixlabs.axelix.master.contract.metadata.ExecutionStats;
import com.axelixlabs.axelix.master.contract.metadata.GarbageCollector;
import com.axelixlabs.axelix.master.contract.metadata.LazyLoadingTarget;
import com.axelixlabs.axelix.master.contract.metadata.PersistenceInsights;
import com.axelixlabs.axelix.master.contract.metadata.TransactionAggregatedProfile;
import com.axelixlabs.axelix.master.contract.metadata.TransactionOrigin;
import com.axelixlabs.axelix.master.contract.metadata.TransactionalKey;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.domain.JavaVersion;
import com.axelixlabs.axelix.master.domain.JdkVendor;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.dashboard.DashboardCache;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.CycloneDxDocuments;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestMetadataFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DashboardApi}.
 *
 * @author Mikhail Polivakha
 * @author Vyacheslav Yanin
 */
public class DashboardApiTest extends AbstractProtectedEndpointTest {

    private static final String instance1Id = UUID.randomUUID().toString();
    private static final String instance2Id = UUID.randomUUID().toString();
    private static final String instance3Id = UUID.randomUUID().toString();

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private DatabaseHistoricalApplicationSnapshotService historicalApplicationSnapshotService;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Autowired
    private DependencyAnalysisService dependencyAnalysisService;

    @Autowired
    private DashboardCache dashboardCache;

    @BeforeEach
    void prepare() {
        dashboardCache.clear();

        // clear instanceRegistry before test
        deRegisterAll();

        // Register instances with different versions and statuses
        registry.register(TestInstanceFactory.create(
                instance1Id,
                "http://example.com/1",
                "test-name",
                Instance.InstanceStatus.UP,
                "25",
                "3.5.2",
                "6.0.2",
                "BellSoft",
                null));

        registry.register(TestInstanceFactory.create(
                instance2Id,
                "http://example.com/2",
                "test-name",
                Instance.InstanceStatus.UP,
                "25",
                "3.5.1",
                "6.0.1",
                "BellSoft",
                "1.9.0"));

        registry.register(TestInstanceFactory.create(
                instance3Id,
                "http://example.com/3",
                "test-name",
                Instance.InstanceStatus.DOWN,
                "17",
                "2.7.0",
                "5.3.0",
                "BellSoft",
                null));
    }

    @AfterEach
    void cleanup() {
        deRegisterAll();
    }

    @Test
    void shouldReturnPersistenceDashboardAggregatedAcrossServices() {
        // given one service with an N + 1 problem (plus a single lazy load that does not qualify) and another
        // with in-memory pagination.
        historicalApplicationSnapshotService.reloadCurrentStateBulk(List.of(
                persistenceMetadata(
                        "com.example", "service-a", List.of(nPlusOne("pets", 4), nPlusOne("category", 1)), Map.of()),
                persistenceMetadata("com.example", "service-b", List.of(), Map.of("owner", 3))));

        // when.
        IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();
        ResponseEntity<String> response = viewer.getForEntity("/api/external/dashboard/persistence", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody())
                .when(IGNORING_ARRAY_ORDER)
                .node("nPlusOne")
                .isEqualTo("[{\"appName\":\"service-a\",\"size\":1}]");
        assertThatJson(response.getBody())
                .when(IGNORING_ARRAY_ORDER)
                .node("inMemoryPagination")
                .isEqualTo("[{\"appName\":\"service-b\",\"size\":1}]");
        assertThatJson(response.getBody()).node("computedAt").isPresent();
        assertSuccessfulCallback(MasterWebEndpoints.DASHBOARD_READ_PERSISTENCE, viewer.getActor());
    }

    @Test
    void shouldReturnSpringPortfolioDashboard() {
        // given a couple of applications with a persisted historical snapshot. The portfolio is aggregated from
        // the latest snapshot per service, not from the live instances table.
        historicalApplicationSnapshotService.reloadCurrentStateBulk(List.of(
                metadata("com.example", "service-a", GarbageCollector.G1),
                metadata("com.example", "service-b", GarbageCollector.ZGC)));

        // when.
        IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();
        ResponseEntity<String> response = viewer.getForEntity("/api/external/dashboard/spring-portfolio", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).node("applicationsTotal").isEqualTo(2);
        assertThatJson(response.getBody()).node("springBoot.platform").isEqualTo(PlatformName.SPRING_BOOT.name());
        assertThatJson(response.getBody())
                .node("springFramework.platform")
                .isEqualTo(PlatformName.SPRING_FRAMEWORK.name());
        assertThatJson(response.getBody()).node("computedAt").isPresent();
        assertSuccessfulCallback(MasterWebEndpoints.DASHBOARD_READ_SPRING_PORTFOLIO, viewer.getActor());
    }

    @Test
    void shouldReturnLanguagesProfileAggregatedAcrossApplications() {
        // given two distinct applications, one Java-only and one that also contains Kotlin, aggregated from the
        // latest snapshot per service.
        deRegisterAll();
        jdbcAggregateTemplate.insertAll(List.of(
                languagesSnapshot("com.example", "a", 25, JdkVendor.BELLSOFT_LIBERICA, null),
                languagesSnapshot("com.example", "b", 21, JdkVendor.ADOPTIUM, "2.1.0")));

        // when.
        IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();
        ResponseEntity<String> response = viewer.getForEntity("/api/external/dashboard/languages", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).node("applicationsOnLts").isEqualTo(2);
        assertThatJson(response.getBody()).node("applicationsOnNonLts").isEqualTo(0);
        assertThatJson(response.getBody())
                .node("languageMix.kotlinApplications")
                .isEqualTo(1);
        assertThatJson(response.getBody()).node("kotlinReleases").isArray().hasSize(1);
        assertThatJson(response.getBody()).node("computedAt").isPresent();
        assertSuccessfulCallback(MasterWebEndpoints.DASHBOARD_READ_LANGUAGES, viewer.getActor());
    }

    @Test
    void shouldReturnDependenciesDashboardAggregatedAcrossApplications() {
        // given.
        dependencyAnalysisService.store(
                registeredInstanceOf("com.example", "service-a"),
                CycloneDxDocuments.of(
                        "com.example:service-a:1.0.0",
                        "org.springframework.cloud:spring-cloud-starter-sleuth:3.1.11",
                        "org.hdrhistogram:HdrHistogram:2.2.2",
                        "org.springframework:spring-core:6.1.5"));
        dependencyAnalysisService.store(
                registeredInstanceOf("com.example", "service-b"),
                CycloneDxDocuments.of(
                        "com.example:service-b:1.0.0",
                        "org.hdrhistogram:HdrHistogram:2.2.2",
                        "io.prometheus:simpleclient:0.16.0",
                        "io.prometheus:simpleclient_common:0.16.0"));
        dependencyAnalysisService.store(
                registeredInstanceOf("com.example", "service-c"),
                CycloneDxDocuments.of(
                        "com.example:service-c:1.0.0",
                        "ch.qos.logback:logback-classic:1.5.18",
                        "org.springframework:spring-core:6.1.5"));

        // when.
        IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();
        ResponseEntity<String> response = viewer.getForEntity("/api/external/dashboard/dependencies", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).node("totalApplications").isEqualTo(3);
        // and then. the application with only an ACTIVE project is the one not at risk.
        assertThatJson(response.getBody())
                .node("applicationsWithRiskyDependencies")
                .isEqualTo(2);
        assertThatJson(response.getBody()).node("applicationsByStatus").isEqualTo("""
                { "SUNSET": 1, "DORMANT": 2, "MAINTENANCE": 1, "ACTIVE": 1 }
                """);
        // and then. spring-core is not curated: it counts as a library, but has no status.
        assertThatJson(response.getBody()).node("totalLibraries").isEqualTo(6);
        assertThatJson(response.getBody()).node("librariesByStatus").isEqualTo("""
                { "SUNSET": 1, "DORMANT": 1, "MAINTENANCE": 2, "ACTIVE": 1 }
                """);
        assertThatJson(response.getBody()).node("riskyLibrariesByEcosystem").isEqualTo("""
                { "OBSERVABILITY": { "SUNSET": 1, "DORMANT": 1, "MAINTENANCE": 2 } }
                """);
        assertThatJson(response.getBody()).node("computedAt").isPresent();
        assertSuccessfulCallback(MasterWebEndpoints.DASHBOARD_READ_DEPENDENCIES, viewer.getActor());
    }

    @Test
    void shouldReturnEmptyDependenciesDashboardWhenNothingRecorded() {
        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().getForEntity("/api/external/dashboard/dependencies", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(response.getBody()).node("totalApplications").isEqualTo(0);
        assertThatJson(response.getBody())
                .node("applicationsWithRiskyDependencies")
                .isEqualTo(0);
        assertThatJson(response.getBody()).node("applicationsByStatus").isEqualTo("{}");
        assertThatJson(response.getBody()).node("totalLibraries").isEqualTo(0);
        assertThatJson(response.getBody()).node("librariesByStatus").isEqualTo("{}");
        assertThatJson(response.getBody()).node("riskyLibrariesByEcosystem").isEqualTo("{}");
        assertThatJson(response.getBody()).node("computedAt").isPresent();
    }

    @Test
    void shouldSkipUnparsableSbomInDependenciesDashboard() {
        // given.
        dependencyAnalysisService.store(
                registeredInstanceOf("com.example", "service-a"),
                CycloneDxDocuments.of("com.example:service-a:1.0.0", "ch.qos.logback:logback-classic:1.5.18"));
        // the store() rejects a malformed SBOM, so the row is inserted directly
        jdbcAggregateTemplate.insert(new ApplicationDependencies(
                UUID.randomUUID().toString(),
                ApplicationId.of("com.example", "service-b"),
                "not a CycloneDX SBOM",
                Instant.now()));

        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().getForEntity("/api/external/dashboard/dependencies", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(response.getBody()).node("totalApplications").isEqualTo(1);
        assertThatJson(response.getBody()).node("totalLibraries").isEqualTo(1);
    }

    @Test
    void shouldReturnUsedLibrariesWithTheirVersionDistribution() {
        // given.
        dependencyAnalysisService.store(
                registeredInstanceOf("com.example", "service-a"),
                CycloneDxDocuments.of(
                        "com.example:service-a:1.0.0",
                        "org.hdrhistogram:HdrHistogram:2.2.2",
                        "com.example.platform:platform-commons:1.9.2"));
        dependencyAnalysisService.store(
                registeredInstanceOf("com.example", "service-b"),
                CycloneDxDocuments.of(
                        "com.example:service-b:1.0.0",
                        "org.hdrhistogram:HdrHistogram:2.1.12",
                        "com.example.platform:platform-commons:1.10.0"));
        dependencyAnalysisService.store(
                registeredInstanceOf("com.example", "service-c"),
                CycloneDxDocuments.of("com.example:service-c:1.0.0", "com.example.platform:platform-commons:1.10.0"));

        // when.
        IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();
        ResponseEntity<String> response =
                viewer.getForEntity("/api/external/dashboard/dependencies/libraries", String.class);

        // then. the risky library comes first, even though the internal one is more widely used.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(response.getBody()).withTolerance(0.01).isEqualTo("""
                {
                  "libraries": [
                    {
                      "library": { "groupId": "org.hdrhistogram", "artifactId": "HdrHistogram" },
                      "applicationCount": 2,
                      "share": 66.67,
                      "versions": [
                        { "version": "2.2.2", "applicationCount": 1 },
                        { "version": "2.1.12", "applicationCount": 1 }
                      ],
                      "status": "DORMANT",
                      "ecosystem": "OBSERVABILITY",
                      "summary": "${json-unit.any-string}",
                      "succession": "${json-unit.ignore}",
                      "reference": "${json-unit.ignore}"
                    },
                    {
                      "library": { "groupId": "com.example.platform", "artifactId": "platform-commons" },
                      "applicationCount": 3,
                      "share": 100.0,
                      "versions": [
                        { "version": "1.10.0", "applicationCount": 2 },
                        { "version": "1.9.2", "applicationCount": 1 }
                      ],
                      "status": null,
                      "ecosystem": null,
                      "summary": null,
                      "succession": null,
                      "reference": null
                    }
                  ],
                  "matchingLibraries": 2,
                  "page": 0,
                  "size": 10
                }
                """);
        assertSuccessfulCallback(MasterWebEndpoints.DASHBOARD_READ_DEPENDENCIES_LIBRARIES, viewer.getActor());
    }

    @Test
    void shouldFilterAndPageUsedLibraries() {
        // given.
        dependencyAnalysisService.store(
                registeredInstanceOf("com.example", "service-a"),
                CycloneDxDocuments.of(
                        "com.example:service-a:1.0.0",
                        "org.springframework.cloud:spring-cloud-starter-sleuth:3.1.11",
                        "org.hdrhistogram:HdrHistogram:2.2.2",
                        "io.prometheus:simpleclient:0.16.0",
                        "io.prometheus:simpleclient_common:0.16.0",
                        "ch.qos.logback:logback-classic:1.5.18"));

        // when.
        ResponseEntity<String> byStatusAndEcosystem = restTemplate
                .asViewer()
                .getForEntity(
                        "/api/external/dashboard/dependencies/libraries"
                                + "?status=MAINTENANCE&status=SUNSET&ecosystem=OBSERVABILITY&page=1&size=2",
                        String.class);
        ResponseEntity<String> bySearch = restTemplate
                .asViewer()
                .getForEntity("/api/external/dashboard/dependencies/libraries?search=LOGBACK", String.class);

        // then.
        assertThatJson(byStatusAndEcosystem.getBody()).node("matchingLibraries").isEqualTo(3);
        assertThatJson(byStatusAndEcosystem.getBody())
                .inPath("libraries[*].library.artifactId")
                .isEqualTo("[\"spring-cloud-starter-sleuth\"]");
        assertThatJson(byStatusAndEcosystem.getBody()).node("page").isEqualTo(1);
        assertThatJson(byStatusAndEcosystem.getBody()).node("size").isEqualTo(2);

        // and then.
        assertThatJson(bySearch.getBody())
                .inPath("libraries[*].library.artifactId")
                .isEqualTo("[\"logback-classic\"]");
    }

    @Test
    void shouldRejectAnInvalidPageOfUsedLibraries() {
        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/dashboard/dependencies/libraries?size=0", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(new TestableMasterWebEndpoint(
                MasterWebEndpoints.DASHBOARD_READ_PERSISTENCE, "/api/external/dashboard/persistence"));
    }

    private void deRegisterAll() {
        jdbcAggregateTemplate.deleteAll(Instance.class);
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
        jdbcAggregateTemplate.deleteAll(ApplicationDependencies.class);
    }

    private static BasicRegistrationMetadata metadata(
            String groupId, String artifactId, GarbageCollector garbageCollector) {
        return TestMetadataFactory.withFeatures(
                groupId, artifactId, false, false, false, false, false, garbageCollector);
    }

    private static BasicRegistrationMetadata persistenceMetadata(
            String groupId,
            String artifactId,
            List<CountedLazyLoadingTarget> lazyLoadingTargets,
            Map<String, Integer> inMemoryPagination) {
        TransactionAggregatedProfile profile = new TransactionAggregatedProfile()
                .transactionOrigin(TransactionOrigin.APPLICATION_DECLARATIVE)
                .transactionalKey(new TransactionalKey()
                        .className("com.example.OwnerService")
                        .methodName("loadOwners"))
                .transactionOverallStats(
                        new ExecutionStats().minMs(1L).maxMs(10L).averageMs(5L))
                .lazyLoadingTargets(lazyLoadingTargets)
                .inMemoryPagination(inMemoryPagination)
                .externalCalls(List.of())
                .propagation("REQUIRED")
                .isolation("DEFAULT")
                .readOnly(false);
        return TestMetadataFactory.withPersistenceInsights(
                groupId, artifactId, new PersistenceInsights().transactions(List.of(profile)));
    }

    private static CountedLazyLoadingTarget nPlusOne(String associationPropertyName, int count) {
        return new CountedLazyLoadingTarget()
                .target(new LazyLoadingTarget()
                        .ownerEntityClass(String.class.getName())
                        .associationPropertyName(associationPropertyName))
                .count(count);
    }

    private static HistoricalApplicationSnapshot languagesSnapshot(
            String groupId, String artifactId, int javaVersion, JdkVendor jdkVendor, @Nullable String kotlinVersion) {
        return new HistoricalApplicationSnapshot(
                new HistoricalApplicationSnapshot.SnapshotId(groupId, artifactId, LocalDate.now(ZoneOffset.UTC)),
                new Insights(
                        new Insights.HotSpot(
                                new Insights.HotSpot.ProjectLeyden(false, false),
                                new Insights.HotSpot.GarbageCollector(
                                        false, com.axelixlabs.axelix.common.domain.insights.GarbageCollector.G1),
                                new Insights.HotSpot.ProjectLilliput(false)),
                        new Insights.SpringFramework(false),
                        new PersistenceInsights().transactions(List.of())),
                new HistoricalApplicationSnapshot.Versions(
                        "1.0.0", "3.5.2", "6.2.1", new JavaVersion(javaVersion, 0, 0, 0), jdkVendor, kotlinVersion));
    }

    private Instance registeredInstanceOf(String groupId, String artifactId) {
        Instance instance = TestInstanceFactory.create(UUID.randomUUID().toString(), groupId, artifactId);
        registry.register(instance);
        return instance;
    }
}
