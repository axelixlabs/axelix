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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.DependencyLibrariesResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.DependencyLibrariesResponse.UsedLibrary;
import com.axelixlabs.axelix.master.api.external.response.dashboard.DependencyLibrariesResponse.VersionUsage;
import com.axelixlabs.axelix.master.api.external.response.dependencies.AnalyzedDependency;
import com.axelixlabs.axelix.master.api.external.response.dependencies.AppFrameworkInfo;
import com.axelixlabs.axelix.master.api.external.response.dependencies.DependencyAnalysisResponse;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformReleaseLine;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.Ecosystem;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SupportStatus;
import com.axelixlabs.axelix.master.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.domain.instance.InstanceSbom;
import com.axelixlabs.axelix.master.exception.SbomNotAvailableException;
import com.axelixlabs.axelix.master.repository.ApplicationDependenciesRepository;
import com.axelixlabs.axelix.master.service.ecosystem.platform.PlatformCatalog;
import com.axelixlabs.axelix.master.service.ecosystem.projects.SoftwareProjectsCatalog;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.ParsedSbom;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.ParsedSbom.ResolvedSbomComponent;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.SbomParser;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.SbomParsingException;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * Default {@link DependencyAnalysisService}.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public class DefaultDependencyAnalysisService implements DependencyAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(DefaultDependencyAnalysisService.class);

    private static final int MAX_LIBRARIES_PAGE_SIZE = 100;

    private final EndpointInvoker endpointInvoker;
    private final ApplicationDependenciesRepository applicationDependenciesRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final SbomParser sbomParser;
    private final SoftwareProjectsCatalog softwareProjectsCatalog;
    private final PlatformCatalog platformCatalog;
    private final InstanceRegistry instanceRegistry;
    private final TransactionTemplate nestedTransaction;

    public DefaultDependencyAnalysisService(
            EndpointInvoker endpointInvoker,
            ApplicationDependenciesRepository applicationDependenciesRepository,
            JdbcAggregateTemplate jdbcAggregateTemplate,
            SbomParser sbomParser,
            SoftwareProjectsCatalog softwareProjectsCatalog,
            PlatformCatalog platformCatalog,
            InstanceRegistry instanceRegistry,
            PlatformTransactionManager transactionManager) {
        this.endpointInvoker = endpointInvoker;
        this.applicationDependenciesRepository = applicationDependenciesRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.sbomParser = sbomParser;
        this.softwareProjectsCatalog = softwareProjectsCatalog;
        this.platformCatalog = platformCatalog;
        this.instanceRegistry = instanceRegistry;
        this.nestedTransaction = new TransactionTemplate(transactionManager);
        this.nestedTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
    }

    @Override
    public DependencyAnalysisResponse analyzeFor(InstanceId instanceId) {
        Instance instance = instanceRegistry.getOrThrow(instanceId);

        ParsedSbom sbom = sbomParser.parse(rawSbomOf(instanceId));

        List<AnalyzedDependency> dependencies = sbom.components().stream()
                .map(component -> new AnalyzedDependency(
                        new AnalyzedDependency.Dependency(component.coordinates(), component.version()),
                        sbom.resolutionPathOf(component),
                        softwareProjectsCatalog.resolve(component.coordinates()).orElse(null)))
                .toList();

        return new DependencyAnalysisResponse(
                sbom.rootCoordinates(), Instant.now(), platformWindowOf(instance), dependencies);
    }

    @Override
    public byte[] getRawSbomFor(InstanceId instanceId) {
        instanceRegistry.getOrThrow(instanceId);

        return rawSbomOf(instanceId);
    }

    @Override
    public void store(Instance instance, String sbom) {
        ApplicationId applicationId = instance.applicationId();

        try {
            sbomParser.parse(sbom.getBytes(StandardCharsets.UTF_8));
        } catch (SbomParsingException e) {
            log.warn("Ignoring the malformed dependency SBOM reported by '{}'", applicationId, e);
            return;
        }

        jdbcAggregateTemplate.upsert(new InstanceSbom(instance.id(), sbom, Instant.now()));

        Optional<ApplicationDependencies> current = applicationDependenciesRepository.findByApplication(
                applicationId.groupId(), applicationId.artifactId());

        if (current.isPresent()) {
            jdbcAggregateTemplate.update(
                    new ApplicationDependencies(current.get().id(), applicationId, sbom, Instant.now()));
            return;
        }

        try {
            // the savepoint keeps the enclosing transaction usable if the insert below fails
            nestedTransaction.executeWithoutResult(_ -> jdbcAggregateTemplate.insert(
                    new ApplicationDependencies(UUID.randomUUID().toString(), applicationId, sbom, Instant.now())));
        } catch (DataAccessException e) {
            log.warn("Unable to store the dependency SBOM of '{}'", applicationId, e);
        }
    }

    // TODO: Drop the fallback to the instance in 1.6, together with ActuatorEndpoints.GET_DEPENDENCIES_SBOM.
    //  Until then, it serves the instances with starters older than 1.3, which do not report the SBOM on registration
    private byte[] rawSbomOf(InstanceId instanceId) {
        InstanceSbom stored = jdbcAggregateTemplate.findById(instanceId, InstanceSbom.class);

        if (stored != null) {
            return stored.sbom().getBytes(StandardCharsets.UTF_8);
        }

        byte[] rawSbom =
                endpointInvoker.invoke(instanceId, ActuatorEndpoints.GET_DEPENDENCIES_SBOM, NoHttpPayload.INSTANCE);

        if (rawSbom.length == 0) {
            throw new SbomNotAvailableException(instanceId);
        }

        return rawSbom;
    }

    @Override
    public DependenciesDashboardResponse getDashboard() {
        LibrariesUsage usage = aggregateLibrariesUsage();

        Map<SupportStatus, Long> librariesByStatus = new EnumMap<>(SupportStatus.class);
        Map<Ecosystem, Map<SupportStatus, Long>> riskyLibrariesByEcosystem = new EnumMap<>(Ecosystem.class);

        for (Library library : usage.applicationsCountByLibrary().keySet()) {
            softwareProjectsCatalog.resolve(library).ifPresent(project -> {
                librariesByStatus.merge(project.status(), 1L, Long::sum);
                if (project.status() != SupportStatus.ACTIVE) {
                    riskyLibrariesByEcosystem
                            .computeIfAbsent(project.ecosystem(), _ -> new EnumMap<>(SupportStatus.class))
                            .merge(project.status(), 1L, Long::sum);
                }
            });
        }

        return new DependenciesDashboardResponse(
                usage.totalApplicationsCount(),
                usage.applicationsWithRiskyDependencies(),
                usage.applicationsCountByStatus(),
                usage.applicationsCountByLibrary().size(),
                librariesByStatus,
                riskyLibrariesByEcosystem,
                Instant.now());
    }

    @Override
    public DependencyLibrariesResponse getLibraries(DependencyLibrariesQuery query) {
        if (query.page() < 0 || query.size() < 1 || query.size() > MAX_LIBRARIES_PAGE_SIZE) {
            throw new BadRequestException(
                    "The page must be at least 0 and the size between 1 and %d".formatted(MAX_LIBRARIES_PAGE_SIZE));
        }

        LibrariesUsage usage = aggregateLibrariesUsage();

        List<UsedLibrary> matching = usage.applicationsCountByLibrary().entrySet().stream()
                .map(entry -> toUsedLibrary(entry.getKey(), entry.getValue(), usage))
                .filter(library -> matches(library, query))
                .sorted(sortResults())
                .toList();

        List<UsedLibrary> page = matching.stream()
                .skip((long) query.page() * query.size())
                .limit(query.size())
                .toList();

        return new DependencyLibrariesResponse(page, matching.size(), query.page(), query.size());
    }

    private static Comparator<UsedLibrary> sortResults() {
        return Comparator.comparing((UsedLibrary library) -> !isRisky(library.status()))
                .thenComparing(
                        Comparator.comparingLong(UsedLibrary::applicationCount).reversed())
                .thenComparing(library -> library.library().toString());
    }

    private UsedLibrary toUsedLibrary(Library library, long applicationCount, LibrariesUsage usage) {
        List<VersionUsage> versions =
                usage.applicationsCountByLibraryVersion().getOrDefault(library, Map.of()).entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByKey(MavenVersionComparator.INSTANCE)
                                .reversed())
                        .map(entry -> new VersionUsage(entry.getKey(), entry.getValue()))
                        .toList();
        double share = (applicationCount * 100.0) / usage.totalApplicationsCount();

        return softwareProjectsCatalog
                .resolve(library)
                .map(project -> new UsedLibrary(
                        library,
                        applicationCount,
                        share,
                        versions,
                        project.status(),
                        project.ecosystem(),
                        project.summary(),
                        project.succession(),
                        project.reference()))
                .orElseGet(() ->
                        new UsedLibrary(library, applicationCount, share, versions, null, null, null, null, null));
    }

    private static boolean matches(UsedLibrary library, DependencyLibrariesQuery query) {
        String search = query.search();
        boolean matchesSearch = search == null
                || search.isBlank()
                || library.library()
                        .toString()
                        .toLowerCase(Locale.ROOT)
                        .contains(search.trim().toLowerCase(Locale.ROOT));

        return matchesSearch
                && (query.statuses().isEmpty() || query.statuses().contains(library.status()))
                && (query.ecosystems().isEmpty() || query.ecosystems().contains(library.ecosystem()));
    }

    private static boolean isRisky(@Nullable SupportStatus status) {
        return status != null && status != SupportStatus.ACTIVE;
    }

    /**
     * Walks every stored SBOM once. A service is counted once per library, and once per version of it.
     */
    private LibrariesUsage aggregateLibrariesUsage() {
        // TODO: We know that findAll() here is bad, especially since each row holds the full SBOM
        //  of the application, which is parsed on every request. We're just making it work now,
        //  then we're going to make it right.
        List<ParsedSbom> sboms = applicationDependenciesRepository.findAll().stream()
                .map(this::parseOrSkip)
                .flatMap(Optional::stream)
                .toList();

        Map<Library, Long> applicationsCountByLibrary = new HashMap<>();
        Map<Library, Map<String, Long>> applicationsCountByLibraryVersion = new HashMap<>();
        Map<SupportStatus, Long> applicationsCountByStatus = new EnumMap<>(SupportStatus.class);
        long applicationsWithRiskyDependencies = 0;

        for (ParsedSbom sbom : sboms) {
            Map<Library, Set<String>> versionsByLibrary = sbom.components().stream()
                    .collect(Collectors.groupingBy(
                            ResolvedSbomComponent::coordinates,
                            Collectors.mapping(ResolvedSbomComponent::version, Collectors.toSet())));

            versionsByLibrary.forEach((library, versions) -> {
                applicationsCountByLibrary.merge(library, 1L, Long::sum);
                Map<String, Long> applicationsByVersion =
                        applicationsCountByLibraryVersion.computeIfAbsent(library, _ -> new HashMap<>());
                versions.forEach(version -> applicationsByVersion.merge(version, 1L, Long::sum));
            });

            Set<SupportStatus> statuses = versionsByLibrary.keySet().stream()
                    .map(softwareProjectsCatalog::resolve)
                    .flatMap(Optional::stream)
                    .map(SoftwareProject::status)
                    .collect(Collectors.toSet());
            statuses.forEach(status -> applicationsCountByStatus.merge(status, 1L, Long::sum));
            if (statuses.stream().anyMatch(DefaultDependencyAnalysisService::isRisky)) {
                applicationsWithRiskyDependencies++;
            }
        }

        return new LibrariesUsage(
                sboms.size(),
                applicationsWithRiskyDependencies,
                applicationsCountByStatus,
                applicationsCountByLibrary,
                applicationsCountByLibraryVersion);
    }

    private record LibrariesUsage(
            long totalApplicationsCount,
            long applicationsWithRiskyDependencies,
            Map<SupportStatus, Long> applicationsCountByStatus,
            Map<Library, Long> applicationsCountByLibrary,
            Map<Library, Map<String, Long>> applicationsCountByLibraryVersion) {}

    private Optional<ParsedSbom> parseOrSkip(ApplicationDependencies application) {
        try {
            return Optional.of(sbomParser.parse(application.sbom().getBytes(StandardCharsets.UTF_8)));
        } catch (SbomParsingException e) {
            log.warn("Skipping the unparsable dependency SBOM of '{}'", application.applicationId(), e);
            return Optional.empty();
        }
    }

    private AppFrameworkInfo platformWindowOf(Instance instance) {
        Platform springBoot = platformCatalog.find(PlatformName.SPRING_BOOT);

        Optional<PlatformReleaseLine> releaseLine = springBoot.lineOf(instance.springBootVersion());

        return new AppFrameworkInfo(
                springBoot.name().name(),
                instance.springBootVersion(),
                releaseLine.orElse(null),
                springBoot.latestKnownReleaseLine(),
                springBoot.minimalOssSupportedReleaseLine());
    }
}
