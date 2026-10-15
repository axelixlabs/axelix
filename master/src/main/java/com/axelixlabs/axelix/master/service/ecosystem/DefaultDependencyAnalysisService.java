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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse.TrackedProject;
import com.axelixlabs.axelix.master.api.external.response.dependencies.AnalyzedDependency;
import com.axelixlabs.axelix.master.api.external.response.dependencies.AppFrameworkInfo;
import com.axelixlabs.axelix.master.api.external.response.dependencies.DependencyAnalysisResponse;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies.ApplicationDependenciesId;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformReleaseLine;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SupportStatus;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.exception.SbomNotAvailableException;
import com.axelixlabs.axelix.master.repository.ApplicationDependenciesRepository;
import com.axelixlabs.axelix.master.service.ecosystem.platform.PlatformCatalog;
import com.axelixlabs.axelix.master.service.ecosystem.projects.SoftwareProjectsCatalog;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.ParsedSbom;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.SbomParser;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.SbomParsingException;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * Default {@link DependencyAnalysisService}.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public class DefaultDependencyAnalysisService implements DependencyAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(DefaultDependencyAnalysisService.class);

    private final EndpointInvoker endpointInvoker;
    private final ApplicationDependenciesRepository applicationDependenciesRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final SbomParser sbomParser;
    private final SoftwareProjectsCatalog softwareProjectsCatalog;
    private final PlatformCatalog platformCatalog;
    private final InstanceRegistry instanceRegistry;

    public DefaultDependencyAnalysisService(
            EndpointInvoker endpointInvoker,
            ApplicationDependenciesRepository applicationDependenciesRepository,
            JdbcAggregateTemplate jdbcAggregateTemplate,
            SbomParser sbomParser,
            SoftwareProjectsCatalog softwareProjectsCatalog,
            PlatformCatalog platformCatalog,
            InstanceRegistry instanceRegistry) {
        this.endpointInvoker = endpointInvoker;
        this.applicationDependenciesRepository = applicationDependenciesRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.sbomParser = sbomParser;
        this.softwareProjectsCatalog = softwareProjectsCatalog;
        this.platformCatalog = platformCatalog;
        this.instanceRegistry = instanceRegistry;
    }

    @Override
    public DependencyAnalysisResponse analyzeFor(InstanceId instanceId) {
        Instance instance =
                instanceRegistry.get(instanceId).orElseThrow(() -> new InstanceNotFoundException(instanceId));

        byte[] rawSbom =
                endpointInvoker.invoke(instanceId, ActuatorEndpoints.GET_DEPENDENCIES_SBOM, NoHttpPayload.INSTANCE);

        if (rawSbom.length == 0) {
            throw new SbomNotAvailableException(instanceId);
        }

        ParsedSbom sbom = sbomParser.parse(rawSbom);

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
    public void store(ApplicationId applicationId, String sbom) {
        try {
            sbomParser.parse(sbom.getBytes(StandardCharsets.UTF_8));
        } catch (SbomParsingException e) {
            log.warn("Ignoring the malformed dependency SBOM reported by '{}'", applicationId, e);
            return;
        }

        jdbcAggregateTemplate.upsert(
                new ApplicationDependencies(ApplicationDependenciesId.of(applicationId), sbom, Instant.now()));
    }

    @Override
    public DependenciesDashboardResponse getDashboard() {
        List<ParsedSbom> sboms = applicationDependenciesRepository.findAll().stream()
                .map(this::parseOrSkip)
                .flatMap(Optional::stream)
                .toList();

        Map<SoftwareProject, Long> servicesByProject = new HashMap<>();
        Map<SupportStatus, Long> servicesByStatus = new EnumMap<>(SupportStatus.class);
        long servicesWithRiskyDependencies = 0;

        for (ParsedSbom sbom : sboms) {
            Set<SoftwareProject> projects = sbom.components().stream()
                    .map(component -> softwareProjectsCatalog.resolve(component.coordinates()))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toSet());

            projects.forEach(project -> servicesByProject.merge(project, 1L, Long::sum));
            Set<SupportStatus> statuses =
                    projects.stream().map(SoftwareProject::status).collect(Collectors.toSet());
            statuses.forEach(status -> servicesByStatus.merge(status, 1L, Long::sum));
            if (statuses.stream().anyMatch(status -> status != SupportStatus.ACTIVE)) {
                servicesWithRiskyDependencies++;
            }
        }

        long totalServices = sboms.size();
        List<TrackedProject> trackedProjects = servicesByProject.entrySet().stream()
                .map(entry -> new TrackedProject(
                        entry.getKey(), entry.getValue(), (entry.getValue() * 100.0) / totalServices))
                .sorted(Comparator.comparingLong(TrackedProject::serviceCount).reversed())
                .toList();

        return new DependenciesDashboardResponse(
                totalServices, servicesWithRiskyDependencies, servicesByStatus, trackedProjects, Instant.now());
    }

    private Optional<ParsedSbom> parseOrSkip(ApplicationDependencies application) {
        try {
            return Optional.of(sbomParser.parse(application.sbom().getBytes(StandardCharsets.UTF_8)));
        } catch (SbomParsingException e) {
            log.warn("Skipping the unparsable dependency SBOM of '{}'", application.applicationDependenciesId(), e);
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
