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
package com.axelixlabs.axelix.master.autoconfiguration.ecosystem;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository;
import com.axelixlabs.axelix.master.service.ecosystem.DefaultDependencyAnalysisService;
import com.axelixlabs.axelix.master.service.ecosystem.DefaultSpringPortfolioService;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;
import com.axelixlabs.axelix.master.service.ecosystem.SpringPortfolioService;
import com.axelixlabs.axelix.master.service.ecosystem.platform.DefaultPlatformCatalog;
import com.axelixlabs.axelix.master.service.ecosystem.platform.PlatformCatalog;
import com.axelixlabs.axelix.master.service.ecosystem.platform.PlatformManifestLoader;
import com.axelixlabs.axelix.master.service.ecosystem.projects.DefaultSoftwareProjectsCatalog;
import com.axelixlabs.axelix.master.service.ecosystem.projects.SoftwareProjectsCatalog;
import com.axelixlabs.axelix.master.service.ecosystem.projects.SoftwareProjectsManifestLoader;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.SbomParser;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * Configuration of the ecosystem analysis: the curated platform and software project catalogs, and the dependency
 * analysis on top of them. The catalogs are built at startup, so a malformed or contradictory curated manifest fails
 * the boot instead of surfacing at request time.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class EcosystemAutoConfiguration {

    @Bean
    public SbomParser sbomParser() {
        return new SbomParser();
    }

    @Bean
    public PlatformManifestLoader platformManifestLoader() {
        return new PlatformManifestLoader();
    }

    @Bean
    public PlatformCatalog platformCatalog(PlatformManifestLoader platformManifestLoader) {
        return new DefaultPlatformCatalog(platformManifestLoader);
    }

    @Bean
    public SoftwareProjectsManifestLoader softwareProjectsManifestLoader() {
        return new SoftwareProjectsManifestLoader(
                new PathMatchingResourcePatternResolver(), SoftwareProjectsManifestLoader.DEFAULT_LOCATION_PATTERN);
    }

    @Bean
    public SoftwareProjectsCatalog softwareProjectsCatalog(
            SoftwareProjectsManifestLoader softwareProjectsManifestLoader) {
        return new DefaultSoftwareProjectsCatalog(softwareProjectsManifestLoader);
    }

    @Bean
    public DependencyAnalysisService dependencyAnalysisService(
            EndpointInvoker endpointInvoker,
            SbomParser sbomParser,
            SoftwareProjectsCatalog softwareProjectsCatalog,
            PlatformCatalog platformCatalog,
            InstanceRegistry instanceRegistry) {
        return new DefaultDependencyAnalysisService(
                endpointInvoker, sbomParser, softwareProjectsCatalog, platformCatalog, instanceRegistry);
    }

    @Bean
    public SpringPortfolioService springPortfolioService(
            HistoricalApplicationSnapshotRepository snapshotRepository, PlatformCatalog platformCatalog) {
        return new DefaultSpringPortfolioService(snapshotRepository, platformCatalog);
    }
}
