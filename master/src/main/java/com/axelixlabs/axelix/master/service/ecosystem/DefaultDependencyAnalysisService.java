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
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.response.dependencies.AnalyzedDependency;
import com.axelixlabs.axelix.master.api.external.response.dependencies.DependencyAnalysisResponse;
import com.axelixlabs.axelix.master.api.external.response.dependencies.FrameworkPlatform;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.exception.SbomNotAvailableException;
import com.axelixlabs.axelix.master.service.ecosystem.platform.PlatformCatalog;
import com.axelixlabs.axelix.master.service.ecosystem.projects.SoftwareProjectsCatalog;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.ParsedSbom;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.SbomParser;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * Default {@link DependencyAnalysisService}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultDependencyAnalysisService implements DependencyAnalysisService {

    private final EndpointInvoker endpointInvoker;
    private final SbomParser sbomParser;
    private final SoftwareProjectsCatalog softwareProjectsCatalog;
    private final PlatformCatalog platformCatalog;
    private final InstanceRegistry instanceRegistry;

    public DefaultDependencyAnalysisService(
            EndpointInvoker endpointInvoker,
            SbomParser sbomParser,
            SoftwareProjectsCatalog softwareProjectsCatalog,
            PlatformCatalog platformCatalog,
            InstanceRegistry instanceRegistry) {
        this.endpointInvoker = endpointInvoker;
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

    private @Nullable FrameworkPlatform platformWindowOf(Instance instance) {
        return platformCatalog
                .find(PlatformName.SPRING_BOOT)
                .flatMap(platform -> platform.lineOf(instance.springBootVersion())
                        .map(line -> new FrameworkPlatform(
                                platform.name(),
                                instance.springBootVersion(),
                                line,
                                platform.latestKnownReleaseLine(),
                                platform.minimalOssSupportedReleaseLine())))
                .orElse(null);
    }
}
